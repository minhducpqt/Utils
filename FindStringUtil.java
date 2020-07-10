package tripi.vn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Luu Minh Duc on 4-July-2020.
 */


public class FindStringUtil{

    enum SearchType {
        SEARCH_TYPE_CONTAIN, // Tìm kiếm chuỗi chứa trong chuỗi
        SEARCH_TYPE_SIMILARITY, // Tìm kiếm gần đúng, so sánh độ tương đồng bằng thuật toán edit distance
        SEARCH_TYPE_FULL_CONTAIN_WITH_START_WORD, // Tìm kiếm chuỗi bắt đầu bởi các ký tự đầu mỗi từ. ví dụ : cong hoa xa hoi, tim kiếm là chxh
        SEARCH_TYPE_FULL_CONTAIN_WITH_ORDER   // Tìm kiếm bằng sự chứa các kí tự của chuỗi input theo đúng thứ tự. ví dụ tìm "cxi" : có trong "conghoaxahoi"
    }

    private static FindStringUtil single_instance = null;

    // static method to create instance of Singleton class
    public static FindStringUtil getInstance()
    {
        if (single_instance == null)
            single_instance = new FindStringUtil();

        return single_instance;
    }

    // S1: longer, S2: shorter. Find S2 in S1
    // Tìm kiếm chuỗi s2 trong chuỗi s1 với các kiểu tìm kiếm được định nghĩa bên trên
        public boolean smartSearchString(String s1, String s2, SearchType type) {
        s1 = prepareInput(s1);
        s2 = prepareInput(s2);
        switch (type) {
            case SEARCH_TYPE_CONTAIN:
                return checkContain(s1, s2);
            case SEARCH_TYPE_SIMILARITY:
                return checkSimilarity(s1, s2);
            case SEARCH_TYPE_FULL_CONTAIN_WITH_START_WORD:
                return checkContainWithStartWord(s1, s2);
            case SEARCH_TYPE_FULL_CONTAIN_WITH_ORDER:
                return checkContainWithOrder(s1, s2);
        }

        return false;
    }

    // Trả về độ giống nhau theo tỉ lệ %. Hàm tìm kiếm chuỗi s2 trong s1
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }


    // Support functions

    // Chuẩn hoá về viết thường và chữ cái tiếng Việt về dạng normal text (tiếng Anh) hết
    private String prepareInput(String str) {
        String standardInputStr = str.toLowerCase();
        standardInputStr = convertNormalText(standardInputStr);
        return standardInputStr;
    }
    private static String convertNormalText(String str) {
        str = str.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
        str = str.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
        str = str.replaceAll("ì|í|ị|ỉ|ĩ", "i");
        str = str.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
        str = str.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
        str = str.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
        str = str.replaceAll("đ", "d");

        str = str.replaceAll("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A");
        str = str.replaceAll("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E");
        str = str.replaceAll("Ì|Í|Ị|Ỉ|Ĩ", "I");
        str = str.replaceAll("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O");
        str = str.replaceAll("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U");
        str = str.replaceAll("Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y");
        str = str.replaceAll("Đ", "D");
        return str;
    }

    private boolean checkContain(String s1, String s2) {
        return  s1.contains(s2);
    }

    private boolean  checkSimilarity(String s1, String s2) {
        if (similarity(s1, s2) > 0.8)
            return true;
        return false;
    }

    private boolean checkContainWithStartWord(String s1, String s2) {
        String allChar = ".*";
        String spaceChar = "\\s";
        String searchPatern = "";
        for (int i = 0; i < s2.length()-1; i++)
        {
            searchPatern = searchPatern.concat(Character.toString(s2.charAt(i)));
            searchPatern = searchPatern.concat(allChar);
            searchPatern = searchPatern.concat(spaceChar);
        }
        searchPatern = searchPatern.concat(Character.toString(s2.charAt(s2.length()-1)));

        Pattern pattern = Pattern.compile(searchPatern);
        Matcher matcher = pattern.matcher(s1);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
    private boolean checkContainWithOrder(String s1, String s2) {
        String allChar = ".*";
        String spaceChar = "\\s";
        String searchPatern = "";
        for (int i = 0; i < s2.length(); i++)
        {
            searchPatern = searchPatern.concat(Character.toString(s2.charAt(i)));
            searchPatern = searchPatern.concat(allChar);
        }

        Pattern pattern = Pattern.compile(searchPatern);
        Matcher matcher = pattern.matcher(s1);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    // Support functions
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }


}
