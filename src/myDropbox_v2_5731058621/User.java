package myDropbox_v2_5731058621;

import com.amazonaws.services.dynamodbv2.datamodeling.*;;

@DynamoDBTable(tableName="myDropboxUsers")
public class User {
	private String username;
	private String password;
	
	@DynamoDBHashKey(attributeName="username")
	public String getUsername() {return username;}
	public void setUsername(String username) {this.username=username;}
	
	@DynamoDBAttribute(attributeName="password")
	public String getPassword() {return password;}
	public void setPassword(String password) {this.password=password;}
	
	@Override
	public String toString() {
		return "username: " + this.username + " pass: " + this.password;
	}
	
	public boolean isValid() {
	    return username != null && password != null;
	}
}
