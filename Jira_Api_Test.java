package day1;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;

import java.io.File;

import org.testng.Assert;

public class Jira_Api_Test {
	
	public static void main(String[] args) {
		
		String message="hi how are you?";
		
		
		RestAssured.baseURI="http://localhost:8081/";
		SessionFilter session = new SessionFilter();
		
		
		//Create Session
		String response = given()
		.header("content-type","application/json")
		.body("{ \"username\": \"yadraj\",\r\n"
				+ " \"password\": \"yadraj\" }")
		.filter(session)
		
		.when()
		.post("rest/auth/1/session")
		.then().log().body()
		.statusCode(200).extract().response().asString();
		
		
		//Add Comment on existing bug
		String commentResponse = given()
		.header("content-type","application/json")
		
		
		.pathParam("id","10100")
		.body("{\r\n"
				+ "    \"body\": \""+message+"\",\r\n"
				+ "    \"visibility\": {\r\n"
				+ "        \"type\": \"role\",\r\n"
				+ "        \"value\": \"Administrators\"\r\n"
				+ "    }\r\n"
				+ "}")
		.filter(session)
		.when()
		.post("rest/api/2/issue/{id}/comment")
		.then()
		.statusCode(201).log().body().extract().response().asString();
		JsonPath js =  new JsonPath(commentResponse);
	String commentId=js.getString("id");
		
		//Add Attachments
		
		given()
		.header("file","multipart/form-data")
		.pathParam("id","10100")
		.filter(session)
		.header("X-Atlassian-Token","no-check")
		.multiPart("file",new File("jira.txt"))
		.when()
		.post("rest/api/2/issue/{id}/attachments")
		.then().log().body().statusCode(200);
		
		
		
		//Get Issue
		
		String getResponse = given()
		.filter(session)
		.pathParam("id","10100")
		.when()
		.get("rest/api/2/issue/{id}")
		.then()
		.statusCode(200)
		.log().all()
		.extract().response().asString();
		System.out.println(getResponse);
		
		
		JsonPath js1 = new JsonPath(getResponse);
		int count=js1.getInt("fields.comment.comments.size()");
		
		for(int i =0; i<count;i++) {
			
			String All_id=js1.get("fields.comment.comments["+i+"].id").toString();
			System.out.println(All_id);
			
			
			if (All_id.equals(commentId)) {
				
				String comment1=js1.get("fields.comment.comments["+i+"].body").toString();
				System.out.println(comment1);
				Assert.assertEquals(comment1,message);
				
				
			}
			
			
			
		}
		
		
		
		
		
		
	}
	
	
	
	
	

}
