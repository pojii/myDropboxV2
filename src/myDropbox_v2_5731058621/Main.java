package myDropbox_v2_5731058621;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import myDropbox_5731058621.User;

public class Main {
	
	//DynamoDB
		static AmazonDynamoDB client = AmazonDynamoDBClientBuilder
				.standard()
				.withRegion(Regions.AP_SOUTHEAST_1)
				.withCredentials(new ProfileCredentialsProvider())
				.build();
		static DynamoDB dynamoDB = new DynamoDB(client);
		static DynamoDBMapper mapper = new DynamoDBMapper(client);
		static String tableName = "UserDropbox";
		
		//S3
		static AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
		        .withRegion(Regions.AP_SOUTHEAST_1)
			    .withCredentials(new ProfileCredentialsProvider())
			    .build();
		static String bucketName = "chula5731058621";
		
		//user state
		static User currentUser = new User();
		

	public static void main(String[] args) throws InterruptedException {
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
		System.out.println("Welcome to myDropbox Application\r\n" + 
				"======================================================\r\n" + 
				"Please input command (newuser username password password, login\r\n" + 
				"username password, put filename, get filename, view, or logout).\r\n" + 
				"If you want to quit the program just type quit.\r\n" + 
				"======================================================");
		
		while(true) {
			String command = input.next();
			if(command.compareToIgnoreCase("newuser")==0) {
				String username = input.next();
				String pass = input.next();
				String confirmPass = input.next();
				if(pass.equals(confirmPass)) {
					addUser(username,pass);
					System.out.println("OK");
				}else {
					System.out.println("Password confirm is Wrong");
				}
				
			}else if(command.compareToIgnoreCase("login")==0) {
				String username = input.next();
				String pass = input.next();
				boolean canLogin = false;
				try {
					 canLogin = login(username, pass);
				}catch(NullPointerException e) {
					System.out.println("Username or password in incorrect. Please try again.");
					continue;
				}
				if(canLogin)System.out.println("OK");
			}else if(command.compareToIgnoreCase("put")==0) {
				if(!currentUser.isValid()) {
					System.out.println("You can't upload file, Please Login first.");
					continue;
				}
				System.out.println("Working Directory = " +
						System.getProperty("user.dir"));
				String fileName = input.next();
				String path = System.getProperty("user.dir")+ "/" + fileName;
				System.out.println("File Directory = " +
						path);
				TransferManager tm = new TransferManager(s3Client);
		        Upload upload = tm.upload(
		           bucketName, (currentUser.getUsername() + " "  +  fileName), new File(path));
				   // Use TransferManager to upload file to S3
			       try {
			    		// Or you can block and wait for the upload to finish
					       	upload.waitForCompletion();
					       	System.out.println("Upload complete.");
			       }catch (AmazonClientException amazonClientException) {
				       	System.out.println("Unable to upload file, upload was aborted. Please check filename.");
				       	//amazonClientException.printStackTrace();
			       }
			}else if(command.compareToIgnoreCase("view")==0) {
				if(!currentUser.isValid()) {
					System.out.println("Please Login first.");
					continue;
				}
				try {
		            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2);
		            ListObjectsV2Result result;
		            boolean haveFile = false;
		            do {               
		               result = s3Client.listObjectsV2(req);
		               
		               for (S3ObjectSummary objectSummary : 
		                   result.getObjectSummaries()) {
		            	   String key = objectSummary.getKey();
		            	   String usernameKey = key.substring(0, key.indexOf(' '));
		            	   String fileName = key.substring(key.indexOf(' ') + 1);
		            	   if(usernameKey.equals(currentUser.getUsername())) {
		            		   System.out.println(fileName + "  " +
			                           objectSummary.getSize() + " " +
			                		   objectSummary.getLastModified() + " " +
			                		   usernameKey
			                           );
		            		   haveFile=true;
		            	   }
		               }
		               
		               //System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
		               req.setContinuationToken(result.getNextContinuationToken());
		            } while(result.isTruncated() == true ); 
		            if(!haveFile)System.out.println("you don't any file in bucket");
		            haveFile=false;
		            
		         } catch (AmazonServiceException ase) {
		            System.out.println("Caught an AmazonServiceException, " +
		            		"which means your request made it " +
		                    "to Amazon S3, but was rejected with an error response " +
		                    "for some reason.");
		            System.out.println("Error Message:    " + ase.getMessage());
		            System.out.println("HTTP Status Code: " + ase.getStatusCode());
		            System.out.println("AWS Error Code:   " + ase.getErrorCode());
		            System.out.println("Error Type:       " + ase.getErrorType());
		            System.out.println("Request ID:       " + ase.getRequestId());
		        } catch (AmazonClientException ace) {
		            System.out.println("Caught an AmazonClientException, " +
		            		"which means the client encountered " +
		                    "an internal error while trying to communicate" +
		                    " with S3, " +
		                    "such as not being able to access the network.");
		            System.out.println("Error Message: " + ace.getMessage());
		        }
			}else if(command.compareToIgnoreCase("get")==0) {
				if(!currentUser.isValid()) {
					System.out.println("Please Login first.");
					continue;
				}
				String fileName = input.nextLine();
				String key = currentUser.getUsername() + fileName;
				System.out.println(key+" key");
				S3Object object = null;
				try {
					object = s3Client.getObject(new GetObjectRequest(bucketName, key));
					InputStream reader = new BufferedInputStream(
							   object.getObjectContent());
					File file = new File(key);
					OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
					int read = -1;
					while ( ( read = reader.read() ) != -1 ) {
					    writer.write(read);
					}
					writer.flush();
					writer.close();
					reader.close();
					System.out.println("OK");
				}catch(Exception e) {
					System.out.println("File not found, please check file name.");
				}
			}else if(command.compareToIgnoreCase("logout")==0) {
				if(!currentUser.isValid()) {
					System.out.println("Please Login first.");
					continue;
				}
				currentUser.setUsername(null);
				currentUser.setPassword(null);
				System.out.println("OK");
			}else if(command.compareToIgnoreCase("quit")==0) {
				System.out.println("======================================================\r\n" + 
						"Thank you for using myDropbox.\r\n" +
						"See you again!" );
				System.exit(0);
			}
		}
	}
	
	private static boolean login(String username,String pass) {
		if(!checkUserExist(username)) {
			System.out.println("username is wrong");
			return false;
		}
		if(!checkPassword(username, pass)) {
			System.out.println("password is wrong");
			return false;
		}
		currentUser.setUsername(username);
		currentUser.setPassword(pass);
		return true;
//		System.out.println(currentUser.toString());
//		System.out.println("'poj' is logined");
	}
	
	private static boolean checkPassword(String username,String pass) {
		User user = mapper.load(User.class, username);
		return user.getPassword().equals(pass);
	}
	
	private static boolean checkUserExist(String username) {
		try {
			User user = mapper.load(User.class, username);
			return true;
		}catch(NullPointerException e) {
			System.out.println("username is not exist");
			return false;
		}
	}
	
	private static void addUser(String username,String pass) {
		Table table = dynamoDB.getTable("myDropboxUsers");
        final Map<String, Object> infoMap = new HashMap<String, Object>();
        infoMap.put("username", username);
        infoMap.put("password", pass);
        try {
            //System.out.println("++Adding a new user...");
            PutItemOutcome outcome = table
                    .putItem(new Item().withPrimaryKey("username", username, "password", pass));
            //System.out.println("++Adding succeeded\n");
        }
        catch (Exception e) {
            System.err.println("Unable to add user: " + username);
            System.err.println(e.getMessage());
        }
	}
	
	private static void getUser(DynamoDBMapper mapper, String username) throws Exception {
        System.out.println("+GetUser: Get user username="+username);
        System.out.println("+User table has no sort key. You can do GetItem, but not Query.");
        //System.out.println(User.class.toString()+" dd");
        User user = mapper.load(User.class, username);
        System.out.format("+Username = %s password = %s, %n", user.getUsername(), user.getPassword());
    }
	

    static void updateExampleTable() {
        Table table = dynamoDB.getTable(tableName);
        System.out.println("Modifying provisioned throughput for " + tableName);
        try {
            table.updateTable(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(2L));
            table.waitForActive();
        }
        catch (Exception e) {
            System.err.println("UpdateTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }

    static void deleteExampleTable() {

        Table table = dynamoDB.getTable(tableName);
        try {
            System.out.println("Issuing DeleteTable request for " + tableName);
            table.delete();

            System.out.println("Waiting for " + tableName + " to be deleted...this may take a while...");

            table.waitForDelete();
        }
        catch (Exception e) {
            System.err.println("DeleteTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }
    
 // Add the object in filePath on my computer to the bucketName bucket on S3, using the key keyName for the object
    // Catch all exceptions, and print error to stdout (System.out)
    public static void addObjectToBucket(AmazonS3 s3Client, String bucketName, String keyName, String filePath) throws InterruptedException {

        TransferManager tm = new TransferManager(s3Client);
        Upload upload = tm.upload(
        		bucketName, keyName, new File(filePath));
        
        // Use TransferManager to upload file to S3
        try {
        	// Or you can block and wait for the upload to finish
        	upload.waitForCompletion();
        	System.out.println("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
        	System.out.println("Unable to upload file, upload was aborted.");
        	amazonClientException.printStackTrace();
        }

    }

    // List all objects in the bucketName bucket
    // Catch all exceptions, and print error to stdout (System.out)
    public static void viewObjectsInBucket(AmazonS3 s3Client, String bucketName) {
    	try {
            System.out.println("Listing objects");
            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2);
            ListObjectsV2Result result;
            do {               
               result = s3Client.listObjectsV2(req);
               
               for (S3ObjectSummary objectSummary : 
                   result.getObjectSummaries()) {
                   System.out.println(" - " + objectSummary.getKey() + "  " +
                           "(size = " + objectSummary.getSize() + 
                           ")");
               }
               System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
               req.setContinuationToken(result.getNextContinuationToken());
            } while(result.isTruncated() == true ); 
            
         } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
            		"which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
            		"which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

    }
	
}


