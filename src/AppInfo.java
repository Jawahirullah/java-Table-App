public class AppInfo {
    
    public static boolean locked;
    public static boolean pwdSet;
    public static String pwd;
    public static String forgetQuestion;
    public static String forgetAnswer;
    public static String appTheme;

    public static void createPassword(String[] data){
        locked = (data[0].equals("true"))?true:false;
        pwdSet = (data[1].equals("true"))?true:false;
        pwd = data[2];
        forgetQuestion = data[3];
        forgetAnswer = data[4];
        appTheme = data[5];

        for(String datas : data)
        {
            System.out.println(datas);
        }
    }
    
    public static void resetPassword()
    {
        locked = false;
        pwdSet = false;
        pwd = "not_set";
        forgetQuestion = "not_set";
        forgetAnswer = "not_set";
    }

    public static void storeAppInfo()
    {
        DBA.setMetaData(locked, pwdSet, pwd, forgetQuestion, forgetAnswer, appTheme);
    }

    public static void setPassword(String password, String question, String answer) {

        pwd = password;
        forgetQuestion = question;
        forgetAnswer = answer;

    }

}
