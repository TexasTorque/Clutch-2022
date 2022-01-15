package org.texastorque.util;

public final class FileUtils {
	
	private FileUtils() { } // Only allow static methods.
//
//	private static final Gson GSON = new GsonBuilder().create();
//	
//
//
//	/** Creates a filepath string with a timestamp attached to the filename.
//	 * 
//	 * File path is formatted as `../dir/filename_timestamp.extension`.
//	 * 
//	 * @param dir The directory that should contain the file. Should be `/dir` and not `/dir/`.
//	 * @param filename Name of the file without an extension.
//	 * @param extension The file extension (e.g. `json`, `xml`, etc.).
//	 * @return
//	 */
//	public static String createTimestampedFilepath(String dir, String filename, String extension) {
//		SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//		String timestamp = timeStampFormat.format(new Date()); // 
//		return String.format("%s/%s.%s", dir, filename, extension);
//	}
//	
//	
//	/** Serializes a java object to XML.
//	 * @param fileLocation The file path used to save the XML.
//	 * @param encodable The XML encodable object to save.
//	 * @return True if the file was saved, false otherwise.
//	 */
//	public static boolean writeToXML(String fileLocation, Object encodable) {
//		try (XMLEncoder writer = new XMLEncoder(new FileOutputStream(fileLocation))) {
//			writer.writeObject(encodable);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
//	
//	/** Deserializes a java object from XML.
//	 * @param fileLocation The file path to the XML to read.
//	 * @param type The class used to cast the XML decoded object.
//	 * @return The type-cast object if successful, null otherwise.
//	 */
//	public static <ObjectType> ObjectType readFromXML(String fileLocation, Class<ObjectType> type) {
//		try (XMLDecoder reader = new XMLDecoder(new FileInputStream(fileLocation))) {
//			return type.cast(reader.readObject());
//		} catch (FileNotFoundException e) {
//			System.out.println(String.format("File not found: %s", fileLocation));
//		}
//		
//		return null;
//	}
//	
//	/** Serializes a java object to JSON.
//	 * @param fileLocation The file path used to save the JSON.
//	 * @param encodable The JSON encodable object to save.
//	 * @return True if the file was saved, false otherwise.
//	 */
//	public static boolean writeToJSON(String fileLocation, Object encodable) {
//		try (Writer writer = new FileWriter(fileLocation)) {
//			GSON.toJson(encodable, writer);
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
//
//	
//	/** Deserializes a java object from JSON.
//	 * @param fileLocation The file path to the JSON to read.
//	 * @param listType The class used to cast the JSON decoded object.
//	 * @return The type-cast object if successful, null otherwise.
//	 */
//	public static <ObjectType> ObjectType readFromJSON(String fileLocation, Type listType) {
//		try (Reader reader = new BufferedReader(new FileReader(fileLocation))) {
//			System.out.println(fileLocation);
//			System.out.println(reader);
//			return GSON.fromJson(reader, listType);
//		} catch (IOException e) {
//			System.out.println(String.format(e.getMessage()+"Could not read JSON from file: %s", fileLocation));
//		}
//		
//		return null;
//	}
//	
}
