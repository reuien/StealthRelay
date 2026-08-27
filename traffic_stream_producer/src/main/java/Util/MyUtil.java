package Util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.ImageIcon;



public class MyUtil {
	private static Scanner scanner  = null;
	public final static String DATEFORMAT="yyyy-MM-dd";
	public final static String DATEFORMAT2="yyyy-MM-dd HH:mm";
	public final static String DATEFORMAT3="yyyy-MM-dd HH:mm:ss";
	public static String consoleScanner() {
		if ( null == scanner) {
			scanner = new Scanner(System.in);
		}
		String input = null;
		try {
			input = scanner.nextLine();
		} catch (Exception e) {
			System.out.println("控制台输入获取异常");
			e.printStackTrace();
		}
		return input;
	}
	
	 // 关闭扫描
	public static void scannerClose() {
		scanner.close();
	}
	
	 //  字符串日期类型转换时间格式
	public static String date2Str ( Date date ) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT3);
		return sdf.format(date); 
	}
	
	 // 字符串类型转换日期类型
	public static Date str2Date ( String datestr ) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT3);
		try {
			return sdf.parse(datestr);
		} catch (ParseException e) {
			System.out.println("时间类型转换错误");
			e.printStackTrace();
		}
		return null;
	}

	public static Date str2DateEnd ( String datestr ) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT3);
		try {
			Date dt = sdf.parse(datestr);
			return new Date(dt.getTime()+999);
		} catch (ParseException e) {
			System.out.println("时间类型转换错误");
			e.printStackTrace();
		}
		return null;
	}
	
	 //实现清屏
	@SuppressWarnings("deprecation")
	public static void clearConsole(){
		try {
			Robot r = new Robot();
			r.mousePress(InputEvent.BUTTON3_MASK);       // 按下鼠标右键
	        r.mouseRelease(InputEvent.BUTTON3_MASK);    // 释放鼠标右键
		    r.keyPress(KeyEvent.VK_R);                    // 按下R键
		    r.keyRelease(KeyEvent.VK_R);
		    r.delay(200);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	//延迟
	public static void thread() {
		 Thread.currentThread();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	//判断电话格式
	public static boolean teltest(String tel) {
		   int a = 0;
			int b = 1;
			if(tel.length()==11) {
				List<Integer> list = new ArrayList<Integer>();
				for(int i=0;i<tel.length();i++) {
					String st = tel.substring(i, i+1);
					list.add(Integer.parseInt(st));
				}
				for(Integer s : list) {
					if(0<=s&&9>=s) {
						a=a+1;
					}else {
						b=0;
					}
				}
				if(a==11||b==1) {
					return true;
				}else {
					return false;
				}
			}else {
				return false;
			}
		}
	
	//判断生日格式
	public static boolean birtest(String bir) {
		if(bir.length()==10) {
			if(isNumeric(bir.substring(0,4))) {
				for(int i =0;i<4;) {
					if(0<=Integer.parseInt((bir.substring(i,i+1)))&&9>=Integer.parseInt((bir.substring(i,i+1)))) {
							if(bir.substring(4,5).equals("-")) {
								if(isNumeric(bir.substring(5,7))) {
										if(1<=Integer.parseInt((bir.substring(5,7)))&&12>=Integer.parseInt((bir.substring(5,7)))) {
												if(bir.substring(7,8).equals("-")) {
													if(isNumeric(bir.substring(8,10))) {
															if(1<=Integer.parseInt((bir.substring(8,10)))&&31>=Integer.parseInt((bir.substring(8,10)))) {
																return true;
															}else {
																return false;
															}
													}else {
														return false;
													}
												}else{
													return false;
												}
										}else {
											return false;
										}
								}else {
									return false;
								}
							}else {
								return false;
							}
					}else {
						return false;
					}
				}
			}else {	
				return false;
				}
			}else {
				return false;
			}
		return false;
	}
	
	//判断输入字符串是否为整数
	public static boolean isNumeric(String str) {
			for(int i=0;i<str.length();i++) {
				if(!Character.isDigit(str.charAt(i))) {
					return false;
				}
			}
			return true;
		}
	
	//判断输入是否为字母字符
	public static boolean isString(String str) {
		for(int i=0;i<str.length();i++) {
			if(!Character.isLowerCase(str.charAt(i))&&!Character.isUpperCase(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	//String转byte[]
    public static byte[] StringToByte(String str) {
    	byte[] byteArray = str.getBytes();
    	return byteArray;
    }

    //字符串转二进制
    public static String toBinary(String str) {
    	char[] strChar=str.toCharArray();
        String result="";
        for(int i=0;i<strChar.length;i++){
            result +=Integer.toBinaryString(strChar[i]);
        }
        return result;
    }
    //二进制转字符串

 // 二进制转字符串

 public static String byte2hex(byte[] b) 

 {
    StringBuffer sb = new StringBuffer();
    String tmp = "";
    for (int i = 0; i < b.length; i++) {
     tmp = Integer.toHexString(b[i] & 0XFF);
     if (tmp.length() == 1){
     	sb.append("0" + tmp);
     }else{
     	sb.append(tmp);
     }
    }
    return sb.toString();
 }

 public static byte[] int2Bytes(int num) {
		byte[] bytes = new byte[4];
		//通过移位运算，截取低8位的方式，将int保存到byte数组
		bytes[0] = (byte)(num >>> 24);
		bytes[1] = (byte)(num >>> 16);
		bytes[2] = (byte)(num >>> 8);
		bytes[3] = (byte)num;
		return bytes;
	}

 


	//int[]转String

	public static String ArrayTransformString(int[] SafetyMeasure) {
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<SafetyMeasure.length;i++){
			  sb.append(SafetyMeasure[i]);
			}
			return sb.toString();
		}
	
	//字符转ASC
	public static int getAsc(String st) {
        byte[] gc = st.getBytes();
         int ascNum = (int) gc[0];
        return ascNum;
    }
	
	//ASC转字符
	public static String backstring(int backnum) {
         char strChar = (char) backnum;
        return String.valueOf(strChar);
    }
	
//随机生成大素数
	public static BigInteger BigPrime(int bitlength) {
		BigInteger bi;
		Random rnd = new Random();
		bi = BigInteger.probablePrime(bitlength, rnd);
        return bi;
	}


 
    //字节数组转16进制
	public static String fromBytesToHex(byte[] resultBytes) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < resultBytes.length; i++) {
			if (Integer.toHexString(0xFF & resultBytes[i]).length() == 1) {
				builder.append("0").append(
						Integer.toHexString(0xFF & resultBytes[i]));
			} else {
				builder.append(Integer.toHexString(0xFF & resultBytes[i]));
			}
		}
		return builder.toString();
	}

	//将16进制转换为二进制
	public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
	public static String toString(String binary) {
		binary=insert(binary);
        String[] tempStr=binary.split(" ");
           char[] tempChar=new char[tempStr.length];
           for(int i=0;i<tempStr.length;i++) {
              tempChar[i]=BinstrToChar(tempStr[i]);
           }
           return String.valueOf(tempChar);
   }


   //将二进制字符串转换成int数组
   public static int[] BinstrToIntArray(String binStr) {       
       char[] temp=binStr.toCharArray();
       int[] result=new int[temp.length];   
       for(int i=0;i<temp.length;i++) {
           result[i]=temp[i]-48;
       }
       return result;
   }


   //将二进制转换成字符
   public static char BinstrToChar(String binStr){
       int[] temp=BinstrToIntArray(binStr);
       int sum=0;
       for(int i=0; i<temp.length;i++){
           sum +=temp[temp.length-1-i]<<i;
       }   
       return (char)sum;
  }





	public static int[] StringToInt(String str) {
		String str1 = String.valueOf(str);
		int l = str1.length();
		int[] num = new int[l];        
		for(int i=0;i<l;i++){
		   num[i] = str1.charAt(i);
		}
		return num;
	}
	
	public static   String insert (String str) {
		StringBuilder sb = new StringBuilder(str);//构造一个StringBuilder对象
		for(int i =7;i<str.length();i=i+8) {
			sb.insert(i, " ");
		}
//        sb.insert(1, " ");//在指定的位置1，插入指定的字符串

        str = sb.toString();
        System.out.println(str);
        return str;
	}
	
	public static String[] dosplit(String string) {
    	String[] result=string.split(",");
    	return result;
	}
    //字符串转字符串数组
	
	
	//算年龄
	public static int getAge(String birthTimeString) {

		// 先截取到字符串中的年、月、日

		String strs[] = birthTimeString.trim().split("-");

		int selectYear = Integer.parseInt(strs[0]);

		int selectMonth = Integer.parseInt(strs[1]);

		int selectDay = Integer.parseInt(strs[2]);

		// 得到当前时间的年、月、日

		Calendar cal = Calendar.getInstance();

		int yearNow = cal.get(Calendar.YEAR);

		int monthNow = cal.get(Calendar.MONTH) + 1;

		int dayNow = cal.get(Calendar.DATE);

 

		// 用当前年月日减去生日年月日

		int yearMinus = yearNow - selectYear;

		int monthMinus = monthNow - selectMonth;

		int dayMinus = dayNow - selectDay;

 

		int age = yearMinus;// 先大致赋值

		if (yearMinus < 0) {// 选了未来的年份

			age = 0;

		} else if (yearMinus == 0) {// 同年的，要么为1，要么为0

			if (monthMinus < 0) {// 选了未来的月份

				age = 0;

			} else if (monthMinus == 0) {// 同月份的

				if (dayMinus < 0) {// 选了未来的日期

					age = 0;

				} else if (dayMinus >= 0) {

					age = 1;

				}

			} else if (monthMinus > 0) {

				age = 1;

			}

		} else if (yearMinus > 0) {

			if (monthMinus < 0) {// 当前月>生日月

			} else if (monthMinus == 0) {// 同月份的，再根据日期计算年龄

				if (dayMinus < 0) {

				} else if (dayMinus >= 0) {

					age = age + 1;

				}

			} else if (monthMinus > 0) {

				age = age + 1;

			}

		}

		return age;

	}
	

	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(Class<T> targetType, Object[] arrayObjects) {

        if (targetType == null) {

            return (T[]) arrayObjects;

        }

        if (arrayObjects == null) {

            return null;

        }

        T[] targetArray = (T[]) Array.newInstance(targetType, arrayObjects.length);

        try {

            System.arraycopy(arrayObjects, 0, targetArray, 0, arrayObjects.length);

        } catch (ArrayStoreException e) {

        	e.printStackTrace();

        }

        return targetArray;

    }
	
	public static Icon setIcon(String path) {
		Icon icon = new ImageIcon(path);
		Image originalImage = ((ImageIcon) icon).getImage();
		Image scaledImage = originalImage.getScaledInstance(23, 23, Image.SCALE_SMOOTH);
		Icon scaledIcon = new ImageIcon(scaledImage);
		return scaledIcon;
	}
	
	
	public enum Precision {

        ONE_SECOND(1000),
        TEN_SECONDS(10000),
        HALF_MINUTE(30000),
        ONE_MINUTE(60000),
        TEN_MINUTES(600000),
        HALF_HOUR(1800000),
        ONE_HOUR(3600000);

        private final long millis;

        Precision(long s) {
            this.millis = s;
        }

        public static List<Precision> getGreaterPrecisions(Precision precision) {

            List<Precision> precisions = new ArrayList<>();
            for (MyUtil.Precision otherPrecision : MyUtil.Precision.values()) {
                if (otherPrecision.getMillis() > precision.getMillis()) {
                    precisions.add(otherPrecision);
                }
            }
            return precisions;
        }

        public long getMillis() {
            return millis;
        }
    }
	
	public static Precision[] getHigherPrecisions(Precision precision) {
	    Precision[] precisions = Precision.values();
	    int selectedOrdinal = precision.ordinal();
	    if (selectedOrdinal == precisions.length - 1) {
	        return new Precision[] { precision };
	    }
	    return Arrays.stream(precisions)
	                 .filter(p -> p.ordinal() > selectedOrdinal)
	                 .toArray(Precision[]::new);
	}
	
	
	
	



}




