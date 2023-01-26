import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class DBA {
    
    private static final String DB_URL="jdbc:mysql://localhost:3306/mydb";
    private static final String USER="USERNAME";
    private static final String PASSWORD="PASSWORD";

    private static Connection connection = null;
    private static Statement statement = null;

    public static void createConnection(){

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {

            if(connection != null)
            {
                connection.close();
            }
                System.out.println("Failed to create Statement");
            }
        }
        catch (SQLException e) {
            System.out.println("Error in creating the Database Connection");
        }

    }

    public static String[] getMetaData()
    {
        String[] data = new String[6];

        try(ResultSet rs = DBA.fetchData("hello_table_meta"))
        {

            while(rs.next())
            {
                for(int i = 0; i < 6; i++)
                {

                    String temp = rs.getObject((i+1)).toString();

                    data[i] = temp;
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return data;
    }

    public static void setMetaData(boolean locked, boolean pwdSet, String pwd, String forgetQuestion, String forgetAnswer, String theme) {
        String islocked = Boolean.toString(locked);
        String pwdset = Boolean.toString(pwdSet);
        

        String query = String.format("update hello_table_meta set islocked = %s, pwdset = %s, pwd = '%s', forgetquestion = '%s', forgetanswer = '%s', apptheme = '%s' where no = 1",islocked, pwdset, formatString(pwd), formatString(forgetQuestion), formatString(forgetAnswer), formatString(theme));
        System.out.println(query);
        executeUpdate(query);
    }


    public static void resetMeta()
    {
        String query = "update hello_table_meta set islocked = false, pwdset = false, pwd = 'not_set', forgetquestion = 'not_set', forgetanswer = 'not_set' where no = 1";
        executeUpdate(query);
    }

    public static Statement getStatement()
    {
        return statement;
    }

    public static void closeConnection(){
        try{
            if(statement != null)
            statement.close();
            if(connection != null)
            connection.close();
        }
        catch(SQLException e)
        {
            System.out.println("Unable to close the connection");
            e.printStackTrace();
        }
    }

    public static ResultSet fetchData(String table)
    {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery("select * from `" + table + "`");
        } catch (SQLException e) {
            System.out.println("Unable to create resultset");
            e.printStackTrace();
        }
        return rs;
    }

    public static ResultSet showTables()
    {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery("show tables");
        } catch (SQLException e) {
            System.out.println("Unable to create resultset");
            e.printStackTrace();
        }
        return rs;
    }

     public static String executeUpdate(String query){

        int i = 0;
        try {
            i = statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Unable to create resultset");
            e.printStackTrace();
        }

        return i+" Rows Changed";
     }


     public static void addRecord(String[] data)
     { 
        String updateQuery = "";
        String tableName = "`" + TableManager.tableName + "`";

        for (int index = 0; index < TableManager.columnTypes.length; index++) {

            if(TableManager.columnTypes[index].equals("DATE"))
            {
                data[index] = formatDate(data[index]);
                
                data[index] = "'"+data[index]+"'";
            }
            else if(TableManager.columnTypes[index].equals("VARCHAR"))
            {
                data[index] = formatString(data[index]);
                data[index] = "'"+data[index]+"'";
            }

            updateQuery += data[index] + ",";
        }

        StringBuffer query = new StringBuffer(updateQuery);
        query = query.deleteCharAt(query.length()-1);
        updateQuery = new String(query);

        String formattedQuery = String.format("insert into %s values(%s)",tableName,updateQuery);
        executeUpdate(formattedQuery);
     }



     public static int deleteRecord(ArrayList<Map<String,String>> rows)
     {
        var varHolder = new Object(){ int deletedRecords = 0; };
        String tableName = "`" + TableManager.tableName + "`";
        
        rows.forEach( map -> 
        {
            String data = "";

            data = formatString(map.get(TableManager.columnHeadings[0]));


            String formattedQuery = String.format("delete from %s where `unique_no` = %s",tableName,data);
            executeUpdate(formattedQuery);
            varHolder.deletedRecords++;

        });

        return varHolder.deletedRecords;
     }

    public static void AlterRow(Map<String, String> row, String newValue, int columnIndex) {
        
        String columnName = "`" + TableManager.columnHeadings[columnIndex] + "`";
        String tableName = TableManager.tableName;

        if(TableManager.columnTypes[columnIndex].equals("VARCHAR"))
        {
            newValue = formatString(newValue);
            newValue = "'" + newValue + "'";
        }

        if(TableManager.columnTypes[columnIndex].equals("DATE"))
        {
            newValue = formatDate(newValue);
            newValue = "'" + newValue + "'";
        }

        String unique_no = row.get(TableManager.columnHeadings[0]);

        String formattedQuery = String.format("update `%s` set %s = %s where `unique_no` = %s",tableName,columnName,newValue,unique_no);
        executeUpdate(formattedQuery);
    }

    public static void createTable(String tableName, Map<String, String> columns) {

        String query = "`unique_no` int primary key,";

        for(Map.Entry<String,String> entry : columns.entrySet()){

            query += "`" + entry.getKey() + "`" + " ";

            String dataType = entry.getValue();

            if(dataType.equals("NUMBER"))
            query += "INT" + ",";
            else if(dataType.equals("LETTERS"))
            query += "VARCHAR(70)" + ",";
            else if(dataType.equals("FLOATING NUMBER"))
            query += "FLOAT(20)" + ",";
            else if(dataType.equals("DATE"))
            query += "DATE" + ",";

        }

        query = query.substring(0, query.length()-1);

        String formattedQuery = String.format("create table %s (%s)",tableName,query);
        executeUpdate(formattedQuery);

    }

    public static void deleteTable(String text) {

        String query = "drop table " + text;
        executeUpdate(query);
    }

    private static String formatDate(String date)
    {
        String str = new String(date);

        StringTokenizer tokenizer = new StringTokenizer(str, "-", false);

        str = "-" + tokenizer.nextToken();
        str = "-" + tokenizer.nextToken() + str;
        str = tokenizer.nextToken() + str;

        return str;

    }

    public static int getMaxValue(String table, String column)
    {
        String query = String.format("select max(`%s`) from %s", column, table);

        ResultSet rs = null;
        int n = 0;
        try {
            rs = statement.executeQuery(query);
            while(rs.next())
            {
                n = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Unable to create resultset");
            e.printStackTrace();
        }

        return n;
        
    }

    private static String formatString(String query)
    {
        if(query.contains("\\"))
        query = query.replaceAll("\\\\", "\\\\\\\\");

        if(query.contains("'"))
        query = query.replaceAll("\'", "\\\\'");

        return query;
    }



}
