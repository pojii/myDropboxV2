package myDropbox_v2_5731058621;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DynamoDB {
	//DynamoDB
	static AmazonDynamoDB client = AmazonDynamoDBClientBuilder
			.standard()
			.withRegion(Regions.AP_SOUTHEAST_1)
			.withCredentials(new ProfileCredentialsProvider())
			.build();
	static DynamoDBMapper mapper = new DynamoDBMapper(client);
}
