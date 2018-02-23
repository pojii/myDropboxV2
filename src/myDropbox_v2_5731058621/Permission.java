package myDropbox_v2_5731058621;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="permission")
public class Permission {
	String objectKey;
	String usernameAllowed;
	
	public Permission(String objectKey,String usernameAllowed) {
		this.objectKey = objectKey;
		this.usernameAllowed = usernameAllowed;
	}
	
	@DynamoDBHashKey(attributeName="objectKey")
	public String getObjectKey() {
		return objectKey;
	}
	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}
	
	@DynamoDBRangeKey(attributeName="usernameAllowed")
	public String getUsernameAllowed() {
		return usernameAllowed;
	}
	public void setUsernameAllowed(String usernameAllowed) {
		this.usernameAllowed = usernameAllowed;
	}
	
}
