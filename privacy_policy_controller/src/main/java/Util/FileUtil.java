package Util;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




//从文件中读写
public class FileUtil<E> {

	
	//读文件
		@SuppressWarnings("resource")
		public List<E> fileRead(String filename) {
		    File f = new File(filename);
		    if (!f.exists()) {
		        try {
		            f.createNewFile();
		        } catch (IOException e) {
		            e.printStackTrace();
		            return null;
		        }
		    }
		    FileInputStream fis = null;
		    ObjectInputStream ois = null;
		    try {
		        fis = new FileInputStream(f);
		        ois = new ObjectInputStream(fis);
		        @SuppressWarnings("unchecked")
		        List<E> readObject = (List<E>) ois.readObject();
		        List<E> root = readObject;
		        return root;
		    } catch (EOFException e1) {

		    } catch (FileNotFoundException e1) {
		        e1.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		    } finally {
		        try {
		            if (ois != null) {
		                ois.close();
		            }
		            if (fis != null) {
		                fis.close();
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		    return null;
		}

		
		//写入文件
		public void fileWrite(List<E> data,String filename)	{
		    File f = new File(filename);
		    ObjectOutputStream oos = null;
		    try {
				FileOutputStream fis = new FileOutputStream(f);
				oos = new ObjectOutputStream(fis);
				oos.writeObject(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    finally
		    {
		    	try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}
		
		//写map进文件
		public static <K, V> void writeMapToFile(Map<K, V> map, String fileName) {
	        ObjectOutputStream oos = null;
	        try {
	            oos = new ObjectOutputStream(new FileOutputStream(fileName));
	            oos.writeObject(map);
	            System.out.println("Map写入文件成功");
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (oos != null) {
	                try {
	                    oos.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

	    @SuppressWarnings("unchecked")
		public static <K, V> Map<K, V> readMapFromFile(String fileName) {
	        Map<K, V> readMap = new HashMap<>();
	        ObjectInputStream ois = null;
	        try {
	            ois = new ObjectInputStream(new FileInputStream(fileName));
	            readMap = (Map<K, V>) ois.readObject();
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        } finally {
	            if (ois != null) {
	                try {
	                    ois.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return readMap;
	    }
	
	
	
	
	//将列表中的信息增添到map中
	public  Map<String,E> loadin_map(List<E> list) {
		Map<String,E> Newmap = new HashMap<>();
		 for(int i=0;i<list.size();i++) {
		        String str =  list.get(i).toString();
		        String[] split=str.split(",");
		        Newmap.put(split[1], list.get(i));
		    }
		 return Newmap;
	}
	
	public  Map<String,E> loadin_namemap(List<E> list) {
		Map<String,E> Newmap = new HashMap<>();
		 for(int i=0;i<list.size();i++) {
		        String str =  list.get(i).toString();
		        String[] split=str.split(",");
		        Newmap.put(split[0], list.get(i));
		    }
		 return Newmap;
	}
	
	public  Map<String,E> loadin_StreamPolicyMap(List<E> list) {
		Map<String,E> Newmap = new HashMap<>();
		 for(int i=0;i<list.size();i++) {
		        String str =  list.get(i).toString();
		        String[] split=str.split(",");
		        Newmap.put(split[1]+"*"+split[0], list.get(i));
		    }
		 return Newmap;
	}
	
	public  Map<Long,E> loadin_PolicyMap_id(List<E> list) {
		Map<Long,E> Newmap = new HashMap<>();
		 for(int i=0;i<list.size();i++) {
		        String str =  list.get(i).toString();
		        String[] split=str.split(",");
		        Newmap.put(Long.parseLong(split[2]), list.get(i));
		    }
		 return Newmap;
	}
	
	
	
	
}