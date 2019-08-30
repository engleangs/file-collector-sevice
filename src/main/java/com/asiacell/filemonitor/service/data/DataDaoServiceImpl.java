package com.asiacell.filemonitor.service.data;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.service.util.UtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class DataDaoServiceImpl implements DataDaoService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${worker-table}")
    private String tableName;
    @Value("${file_final_move_table}")
    private String fileMoveTableName;
    @Value("${file_processing_table}")
    private String fileProcessingTable;
    @Value("${file_not_found_table}")
    private String fileNotFoundTable;
    @Value("${file_service_table}")
    private String fileServiceTable;
    @Value("${file_not_found_all}")
    private String fileNotFoundAllTable;
    @Autowired
    private UtilService utilService;

    @Override
    public List<ProcessItem> getListItem(Collection<String>itemId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(tableName);
        sqlBuilder.append(" WHERE status=0 ");
        if( itemId.size() > 0) {
            sqlBuilder.append(" AND guid NOT IN (");
            String join = "";
            for(String id :itemId) {
                sqlBuilder.append(join)
                        . append("'").append( id).append("'");
                join =",";
            }
            sqlBuilder.append(" )");
        }


        String sql = sqlBuilder.toString();

        List<ProcessItem> items = jdbcTemplate.query(sql, new ProccessItemMapper());
        return items;
    }

    private class ProccessItemMapper implements RowMapper<ProcessItem> {

        @Override
        public ProcessItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            String guid = rs.getString("guid");
            String hostname = rs.getString("from_hostname");
            String folder = rs.getString("folder");
            String msisdn = rs.getString("msisdn");
            String fileName = rs.getString("file_name");
            int status = rs.getInt("status");
            Date addedDate = rs.getTimestamp("added_date");
            return new ProcessItem(guid, hostname, folder, msisdn, fileName, status, addedDate);
        }

    }

    @Override
    public int addBatchFileItems(List<FileMoveItem> fileMoveItems) {
        StringBuilder sqlBuilder = new StringBuilder(" INSERT INTO ").append(fileMoveTableName);
        sqlBuilder.append("(guid,msisdn,temp_path,final_path,file_name,status,description,retry,added_date,last_action_date,action_hostname,start_date,finish_date,take)")
                .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        String query = sqlBuilder.toString();
        List<String> processItemGuid = new ArrayList<>();
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FileMoveItem item = fileMoveItems.get(i);
                long take =   item.getFinish().getTime() - item.getStart().getTime();
                processItemGuid.add("'" + item.getProcessItem().getGuid() + "'");
                ps.setString(1, item.getProcessItem().getGuid());
                ps.setString(2, item.getProcessItem().getMisisdn());
                ps.setString(3, item.getProcessItem().getTempPath());
                ps.setString(4, item.getFinalPath());
                ps.setString(5, item.getProcessItem().getFileName());
                ps.setInt(6, item.getProcessItem().getStatus());
                ps.setString(7, item.getProcessItem().getDescription());
                ps.setInt(8, item.getRetryTime());
                ps.setTimestamp(9, new java.sql.Timestamp(item.getProcessItem().getAddDate().getTime()));
                ps.setTimestamp(10, new java.sql.Timestamp(item.getLastActionDate().getTime()));
                ps.setString( 11,utilService.getHostname());
                ps.setTimestamp( 12, new Timestamp( item.getStart().getTime()));
                ps.setTimestamp( 13, new Timestamp( item.getFinish().getTime()));
                ps.setLong( 14, take);
            }

            @Override
            public int getBatchSize() {
                return fileMoveItems.size();
            }
        });

        sqlBuilder = new StringBuilder(" DELETE FROM ").append(fileProcessingTable).append(" WHERE guid IN (");
        sqlBuilder.append(processItemGuid.get(0));
        for (int i = 1; i < processItemGuid.size(); i++) {
            sqlBuilder.append(",").append(processItemGuid.get(i));
        }
        sqlBuilder.append(")");
        int count = jdbcTemplate.update(sqlBuilder.toString());
        System.out.println("updated count for delete from processing : " + count);
        return 1;
    }


    @Override
    public int addBatchProcessItems(List<ProcessItem> processItems) {
        StringBuilder sqlBuilder = new StringBuilder(" INSERT INTO ").append(fileProcessingTable);
        sqlBuilder.append("(guid,from_hostname,folder,msisdn,file_name,status,added_date)")
                .append(" VALUES(?,?,?,?,?,?,?)");
        String query = sqlBuilder.toString();
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProcessItem item = processItems.get(i);
                ps.setString(1, item.getGuid());
                ps.setString(2, item.getFromHostname());
                ps.setString(3, item.getFolder());
                ps.setString(4, item.getMisisdn());
                ps.setString(5, item.getFileName());
                ps.setInt(6, item.getStatus());
                ps.setTimestamp(7, new java.sql.Timestamp(item.getAddDate().getTime()));
            }

            @Override
            public int getBatchSize() {
                return processItems.size();
            }
        });
        return 1;
    }


    @Override
    public int checkFileNotFoundDb(String hostname, ProcessItem processItem) {
        StringBuilder sqlBuilder = new StringBuilder(" SELECT COUNT(*) total FROM  ");
        sqlBuilder.append(fileNotFoundTable).append(" WHERE hostname=? AND file_name=? AND msisdn =?");
        String query = sqlBuilder.toString();
        Map<String, Object> result = jdbcTemplate.queryForMap(query, hostname, processItem.getFileName(), processItem.getMisisdn());
        return Integer.valueOf(result.get("total") + "");
    }

    @Override
    public void trackFileNotFound(String hostname, ProcessItem processItem) {
        StringBuilder sqlBuilder = new StringBuilder(" INSERT INTO ");
        sqlBuilder.append(fileNotFoundTable).append("(guid,file_name,on_hostname,msisdn,added_date) VALUES(?,?,?,?,?) ");
        String query = sqlBuilder.toString();
        jdbcTemplate.update(query, utilService.guid(), processItem.getFileName(), hostname, processItem.getMisisdn(), new Date());
    }

    @Override
    public void trackOwnService() {
        StringBuilder sqlBuilder = new StringBuilder(" SELECT COUNT(*) total FROM ");
        sqlBuilder.append(fileServiceTable).append(" WHERE hostname=?");
        String query = sqlBuilder.toString();
        Map<String, Object> result = jdbcTemplate.queryForMap(query, utilService.getHostname());
        long total = (long) result.get("total");
        if (total == 0) {
            sqlBuilder = new StringBuilder(" INSERT INTO ");
            sqlBuilder.append(fileServiceTable).append("(hostname,last_update) VALUES(?,?)");
            jdbcTemplate.update(sqlBuilder.toString(), utilService.getHostname(), new Date());
        } else {
            sqlBuilder = new StringBuilder(" UPDATE ");
            sqlBuilder.append(fileServiceTable).append(" SET last_update=? WHERE hostname=?");
            jdbcTemplate.update(sqlBuilder.toString(), new Date(), utilService.getHostname());
        }

    }

    @Override
    public List<String> getAllHostForFileNotFound(ProcessItem processItem) {
        StringBuilder sqlBuilder = new StringBuilder(" SELECT on_hostname FROM ").append(fileNotFoundTable)
                .append(" WHERE file_name=? AND msisdn=?");
        String query = sqlBuilder.toString();
        List<String> result = new ArrayList<>();
        jdbcTemplate.query(query, new Object[]{processItem.getFileName(), processItem.getMisisdn()},  (rs) -> {
            result.add(rs.getString("on_hostname"));
        });
        return result;
    }


    @Override
    public Set<String> getAllServiceHost() {
        StringBuilder sqlBuilder = new StringBuilder(" SELECT DISTINCT hostname FROM ").append(fileServiceTable);
        Set<String> result = new HashSet<>();
        String query = sqlBuilder.toString();
        jdbcTemplate.query(query, rs -> {
            result.add(rs.getString("hostname"));
        });
        return result;
    }

    @Override
    public int moveToNotFound(ProcessItem processItem) {
        StringBuilder sqlBuilder = new StringBuilder(" INSERT INTO ").append(fileNotFoundAllTable)
                .append("(guid,file_name,from_hostname,folder,msisdn,added_date,notify_date)")
                .append(" VALUES(?,?,?,?,?,?,?)");
        String query = sqlBuilder.toString();
        int count = jdbcTemplate.update(query, processItem.getGuid(), processItem.getFileName(), processItem.getFromHostname(),
                processItem.getFolder(), processItem.getMisisdn(), processItem.getAddDate(), new Date());
        if (count > 0) {
            sqlBuilder = new StringBuilder(" DELETE FROM ").append(fileProcessingTable).append(" WHERE guid=?");
            count = jdbcTemplate.update(sqlBuilder.toString(), processItem.getGuid());
        }
        return count;

    }


}
