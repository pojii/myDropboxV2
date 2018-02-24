package myDropbox_v2_5731058621;

import myDropbox_v2_5731058621.DynamoDB;
import myDropbox_v2_5731058621.S3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthSeparatorUI;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import myDropbox_v2_5731058621.User;

public class Main {
	
	//user state
	static User currentUser = new User();
		
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
		System.out.println(
				"Welcome to myDropbox Application\r\n" + 
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
				TransferManager tm = new TransferManager(S3.s3Client);
		        Upload upload = tm.upload(
		           S3.bucketName, (currentUser.getUsername() + " "  +  fileName), new File(path));
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
					Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
					eav.put(":username",new AttributeValue().withS(currentUser.getUsername()));

					DynamoDBQueryExpression<Permission> queryExpression = new DynamoDBQueryExpression<Permission>() 
						.withConsistentRead(false)
					    .withKeyConditionExpression("username = :username")
					    .withExpressionAttributeValues(eav);

					List<Permission> latestReplies = DynamoDB.mapper.query(Permission.class, queryExpression);
					
		            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(S3.bucketName).withMaxKeys(2);
		            ListObjectsV2Result result;
		            boolean haveFile = false;
		            do {               
		               result = S3.s3Client.listObjectsV2(req);
		               for (S3ObjectSummary objectSummary : 
		                   result.getObjectSummaries()) {
		            	   String key = objectSummary.getKey();
		            	   String usernameKey = key.substring(0, key.indexOf(' '));
		            	   String fileName = key.substring(key.indexOf(' ')+1 , key.length());
		            	   if(usernameKey.equals(currentUser.getUsername())) {
		            		   System.out.println(fileName + "  " +
			                           objectSummary.getSize() + " " +
			                		   objectSummary.getLastModified() + " " +
			                		   usernameKey
			                           );
		            		   haveFile=true;
		            	   }
		               }
		               for (S3ObjectSummary objectSummary:
		            		   result.getObjectSummaries()) {
		            	   String key = objectSummary.getKey();
		            	   String usernameKey = key.substring(0, key.indexOf(' '));
		            	   String fileName = key.substring(key.indexOf(' ')+1 , key.length());
		            	   for (Permission permission:latestReplies) {
		            		   if(permission.getObjectKey().equals(key)) {
		            			   System.out.println(fileName + "  " +
				                           objectSummary.getSize() + " " +
				                		   objectSummary.getLastModified() + " " +
				                		   usernameKey
				                           );
		            			   haveFile=true;
		            		   }
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
				String fileName = input.next();
				String userName = input.next();
				String key = userName + " " + fileName;
				
				Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
				eav.put(":username", new AttributeValue().withS(currentUser.getUsername()));
				
				DynamoDBQueryExpression<Permission> queryExpression = new DynamoDBQueryExpression<Permission>() 
					    .withKeyConditionExpression("username = :username")
					    .withExpressionAttributeValues(eav);
				List<Permission> permissions = DynamoDB.mapper.query(Permission.class, queryExpression);
				
				
				
				if(userName.equals(currentUser.getUsername())||containsKey(permissions, key)) {
					S3Object object = null;
					try {
						object = S3.s3Client.getObject(new GetObjectRequest(S3.bucketName, key));
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
				}else {
					System.out.println("You don't have Permission");
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
				System.out.println(
						"======================================================\r\n" + 
						"Thank you for using myDropbox.\r\n" +
						"See you again!" );
				System.exit(0);
			}else if(command.compareToIgnoreCase("share")==0) {
				if(!currentUser.isValid()) {
					System.out.println("Please Login first.");
					continue;
				}
				String fileName = input.next();
				String shareUsername = input.next();
				boolean isOk = addPermission(shareUsername, fileName);
				if(isOk)System.out.println("OK");
			}
		}
	}
	
	public static boolean containsKey(final List<Permission> list, final String key){
	    return list.stream().filter(o -> o.getObjectKey().equals(key)).findFirst().isPresent();
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
	}
	
	private static boolean checkPassword(String username,String pass) {
		User user = DynamoDB.mapper.load(User.class, username);
		return user.getPassword().equals(pass);
	}
	
	private static boolean checkUserExist(String username) {
		try {
			DynamoDB.mapper.load(User.class, username);
			return true;
		}catch(NullPointerException e) {
			System.out.println("username is not exist");
			return false;
		}
	}
	
	private static boolean addPermission(String shareUsername,String fileName) {
		Permission permis = new Permission(currentUser.getUsername()+" "+fileName,shareUsername);
		DynamoDB.mapper.save(permis);
		return true;
	}
	
	private static void addUser(String username,String pass) {
		User newuser = new User();
		newuser.setUsername(username);
		newuser.setPassword(pass);
		DynamoDB.mapper.save(newuser);
	}
}


