--S3.java--
S3 file to setup about S3 client and bucketname
addObjectToBucket(AmazonS3 s3Client, String bucketName, String keyName, String filePath)
	to add file to bucket
viewObjectsInBucket(AmazonS3 s3Client, String bucketName) {
	to view file form bucket
	
-- User.java --
this is user from myDropboxUser table in DynamoDB

-- Permission.java--
permission table in DynamoDB

-- DynamoDB.java --
setup mapper and client in dynamodb

-- main.java --
containsKey(final List<Permission> list, final String key){
	check file permission that you have permisson or not
login(String username,String pass)
	login 
checkPassword(String username,String pass)
	check password is can login or not
addPermission(String shareUsername,String fileName)
	add permission file (share file to other)
addUser(String username,String pass) 
	register new user
	
	
**how to run**
- create user
	newuser 'username' 'password' 'confirmpassword'
-login 
	login 'username' 'password'
-view 
	view file that have permission
- get 
	download file to project directory so file name is ownername and filename example
	"poj example.txt"
- logout 
	logout user
- share 
	share own file to other user
	example "share example.txt poj"
