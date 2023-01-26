import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.util.Callback;

public class TableManager {

    public static TableView<Map> tableView = null;
    public static Callback<TableColumn<Map,String>,TableCell<Map,String>> callback = null;
    public static String tableName = "";
    public static List<String> allTables = new ArrayList<>();
    public static String[] columnTypes = null;
    public static String[] columnHeadings = null;
    public static int maxno = 0;


    public static boolean showTableView(String table)
    {

        if(table.equals(tableName))
        {
            return false;
        }

        if(!tableView.isVisible())
        tableView.setVisible(true);

        tableView.getColumns().clear();
        tableView.getItems().clear(); 
        
        tableName = table;

        try(ResultSet rs = DBA.fetchData(tableName)){
            ResultSetMetaData meta = rs.getMetaData();

            columnHeadings = new String[meta.getColumnCount()];
            columnTypes = new String[meta.getColumnCount()];
            
            
            for (int index = 0; index < columnHeadings.length; index++) {
                columnHeadings[index] = meta.getColumnName(index+1).toLowerCase();
                columnTypes[index] = meta.getColumnTypeName(index+1);
            }

            int j=0;

            while (j<meta.getColumnCount()) {
                TableColumn<Map,String> column = new TableColumn<>(columnHeadings[j].toUpperCase());
                column.setCellValueFactory(new MapValueFactory<String>(columnHeadings[j]));
                column.setResizable(true);
                
                tableView.getColumns().addAll(column);

                if(j == 0)
                {
                    callback = column.getCellFactory();
                    column.setVisible(false);
                }
                j++;
            }

            ObservableList<Map<String,String>> data = FXCollections.<Map<String,String>>observableArrayList();
            int k=0;
            while(rs.next())
            {
                Map<String,String> map = new HashMap<>();
                for (int i= 0; i < meta.getColumnCount(); i++) {
                    
                    String str;
                    
                    try{
                    str = rs.getObject(i+1).toString();
                    }
                    catch(SQLException e)
                    {
                        str="00-00-0000";
                    }

                    if(columnTypes[i].equals("DATE"))
                    {
                    StringTokenizer tokenizer = new StringTokenizer(str, "-", false);
                   
                    str ="-" + tokenizer.nextToken();
                    str ="-" + tokenizer.nextToken() + str;
                    str =tokenizer.nextToken() + str;
                    }

                    map.put(columnHeadings[i],str);

                }
                data.add(map);
            }
            maxno = DBA.getMaxValue(tableName, columnHeadings[0]);
            System.out.println(maxno);
            tableView.getItems().addAll(data);
            setSortOption();

        }
        catch(SQLException e)
        {
            System.out.println("Unable to fetch records from table");
            e.printStackTrace();
        }

        return true;
    }

    public static boolean isSameTable(String table)
    {
        return (tableName).equals(table);
    }

    public static void addRow(String[] datas) {
        
        Map<String,String> map = new HashMap<String,String>();
        for (int i= 0; i < datas.length; i++) {
            map.put(columnHeadings[i],datas[i]);
        }
        tableView.getItems().add(map);
    }

    public static int deleteRow() {
        
        ObservableList<Map> selectedRows = tableView.getSelectionModel().getSelectedItems();

        ArrayList<Map<String,String>> rows = new ArrayList<>(selectedRows);
        var maxValueHolder = new Object(){ int no = maxno; };
        rows.forEach(row -> {

            int n = Integer.parseInt(row.get("unique_no"));
            if(maxValueHolder.no == n)
            {
                --maxValueHolder.no;
            }
            tableView.getItems().remove(row);
        });

        maxno = maxValueHolder.no;
        int deletedRows = DBA.deleteRecord(rows);
        
        return deletedRows;

    }

    public static int getColumnIndex(String colTitle) {

        int index = 0;

        for(int i = 0; i < columnHeadings.length; i++ )
        {
            if(colTitle.equalsIgnoreCase(columnHeadings[i]))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    public static void removeTable(String text) {

        for(String element : allTables)
        {
            if(element.equalsIgnoreCase(text))
            {
                allTables.remove(element);

                //if currently showing table is same as deleted table

                if(TableManager.tableName.equalsIgnoreCase(text))
                {
                    tableView.getColumns().clear();
                    tableName = "";
                    columnTypes = null;
                    columnHeadings = null; 
                }
                break;
            }
        }
    }

    public static void setSortOption() {

     ObservableList<TableColumn<Map, ?>> list = tableView.getColumns();

     for(int i = 1; i < list.size(); i++)
     {
        TableColumn<Map, String> column = (TableColumn<Map, String>) list.get(i);

        if(columnTypes[i].equals("INT"))
        {
            column.setComparator((a, b) -> {
            int no1 = Integer.parseInt(a);
            int no2 = Integer.parseInt(b);

            return Integer.compare(no1, no2);
            });
        }
        else if(columnTypes[i].equals("FLOAT"))
        {
            column.setComparator((a, b) -> {
            Float no1 = Float.parseFloat(a);
            float no2 = Float.parseFloat(b);

            return Float.compare(no1, no2);
            });
        }
        else if(columnTypes[i].equals("DATE"))
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            column.setComparator((a, b) -> {
            Date date1 = null;
            Date date2 = null;
            try {
                date1 = dateFormat.parse(a);
                date2 = dateFormat.parse(b);
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Error in parsing dates");
            }

            return date1.compareTo(date2);
            });
        }

     }

    }
        
}
