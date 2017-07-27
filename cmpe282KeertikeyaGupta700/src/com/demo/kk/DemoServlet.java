package com.demo.kk;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import com.mongodb.MongoException;

import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.math.*;



/**
 * Servlet implementation class DemoServlet
 */
@WebServlet("/DemoServlet")
public class DemoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public DemoServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		String xml = request.getParameter("content");
		String url[] = request.getParameter("url").split("/");
		String resource = request.getParameter("url").replace("CMPE282KeertikeyaGupta700/rest/", "");
		String title = "Demo Servlet";
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";
		String objIDs[] = resource.split("/");
		int l = objIDs.length;
		
		PrintWriter pw = response.getWriter();
		
		System.out.println(url[0] + " : " + url[2]);
		
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		
		//If database present
		if(mongoClient.getDatabaseNames().contains(url[0])){
			
			
			
			DB db = mongoClient.getDB(url[0]);
			
			
			if(db.getCollection(url[2]) != null){
				DBCollection col = db.getCollection(url[2]);
				BasicDBObject obj = new BasicDBObject();
				
				
				if(request.getParameter("url").contains(url[0] + "/rest/")){
					
					
					//GET Method
					if(request.getParameter("method").equalsIgnoreCase("get")){
						String result = "";
						if(l==1){
							DBCursor cursor = col.find();
							
							while(cursor.hasNext()){
								result = result.concat("\n\n" + cursor.next().toString());
							}
							
							pw.println(docType + 
								"<html>\n" +
								"<head><title>" + title + "</title></head>\n" +
								"<body>" +
								"<h1>Status: 200 OK</h1><br/><p>" + result +
								"</p></body>" +
								"</html>"
							);
						}
						else if(l==2){
							int ID = 0;
							String id = objIDs[1];
							
							if(NumberUtils.isNumber(id)){
								ID = Integer.parseInt(id);
							}
							else{
								response.sendError(404, "Resource not found: Document ID does not numeric");
							}
							
							if(ID <= 0){
								response.sendError(404, "Resource not found: Document ID < 0");
							}
							else{
								BasicDBObject finderObject = new BasicDBObject();
								finderObject.put("_id", ID);
								
								DBCursor cursor = col.find(finderObject);
								
								if(cursor.hasNext()){
									while(cursor.hasNext()){
										result = result.concat(cursor.next().toString());
									}
									
									//response.sendError(200, "OK");
									
									pw.println(docType + 
										"<html>\n" +
										"<head><title>" + title + "</title></head>\n" +
										"<body>" +
										"<h1>Status: 200 OK</h1><br/><p>" + result +
										"</p></body>" +
										"</html>"
									);
									
								}
								else{
									response.sendError(404, "Resource not found: No such Document exists");
								}
							}
						}
						else{
							response.sendError(501, "Number of arguments should be 1 or 2");
						}
					}
					
					
					//PUT METHOD
					else if(request.getParameter("method").equalsIgnoreCase("put")){
						//PUT XML content
						
						String x = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xml;
						System.out.println("Pasring XML file...\n");
						
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						try {
							DocumentBuilder builder = factory.newDocumentBuilder();
							Document doc = builder.parse(new InputSource(new ByteArrayInputStream(x.getBytes("utf-8"))));
							
							NodeList empList = null;
							if(url[2].equalsIgnoreCase("employee")){
								empList = doc.getElementsByTagName("employee"); //this is the entire xml content
							}
							
							else if(url[2].equalsIgnoreCase("project")){
								empList = doc.getElementsByTagName("project"); //this is the entire xml content
							}
							
							for(int i=0; i< empList.getLength(); i++){
								Node emp = empList.item(i);		//emp is employee
								
								if(emp.getNodeType()==Node.ELEMENT_NODE){
									Element e = (Element) emp;					// convert from node to element
									NodeList childList = e.getChildNodes();		//this list contains the child nodes of employee
									for(int j=0; j<childList.getLength(); j++){
										Node childNode = childList.item(j);
										
										if(childNode.getNodeType()==Node.ELEMENT_NODE){
											Element id = (Element) childNode;
											System.out.println(id.getTagName() + " = " + id.getTextContent());
											if(NumberUtils.isNumber(id.getTextContent())){
												obj = obj.append(id.getTagName(), Integer.parseInt(id.getTextContent()));
											}
											else{
												obj = obj.append(id.getTagName(), id.getTextContent());
											}
										}
									}
								}
							}
							
							
							try {
								col.insert(obj);
							} catch (Exception e) {
								response.sendError(409, "Duplicate Key Exception: " + e.toString());
							}
							
														
							
							//response.sendError(201, "Created");
							
							
							pw.println(docType + 
								"<html>\n" +
								"<head><title>" + title + "</title></head>\n" +
								"<body>" +
								"<h1>Status 201: Created</h1>" +
								"</body>" +
								"</html>"
							);
							
							
						} catch (ParserConfigurationException e) {
							response.sendError(400, "Parser Configuration Exception: " + e.toString());
							e.printStackTrace();
						} catch (SAXException e) {
							response.sendError(400, "SAX Exception: " + e.toString());
							e.printStackTrace();
						} catch (IOException e) {
							response.sendError(400, "IO Exception: " + e.toString());
							e.printStackTrace();
						}
					}
					
					
					//POST METHOD
					else if(request.getParameter("method").equalsIgnoreCase("post")){
						int replaceID = 0;
						String x = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xml;
						System.out.println("Pasring XML file...\n");
						
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						
						try {
							DocumentBuilder builder = factory.newDocumentBuilder();
							Document doc = builder.parse(new InputSource(new ByteArrayInputStream(x.getBytes("utf-8"))));
							
							NodeList empList = null;
							if(url[2].equalsIgnoreCase("employee")){
								empList = doc.getElementsByTagName("employee"); //this is the entire xml content
							}
							
							else if(url[2].equalsIgnoreCase("project")){
								empList = doc.getElementsByTagName("project"); //this is the entire xml content
							}
							
							for(int i=0; i< empList.getLength(); i++){
								Node emp = empList.item(i);		//emp is employee
								
								if(emp.getNodeType()==Node.ELEMENT_NODE){
									Element e = (Element) emp;					// convert from node to element
									NodeList childList = e.getChildNodes();		//this list contains the child nodes of employee
									
									for(int j=0; j<childList.getLength(); j++){
										Node childNode = childList.item(j);
										
										if(childNode.getNodeType()==Node.ELEMENT_NODE){
											Element id = (Element) childNode;
											System.out.println(id.getTagName() + " = " + id.getTextContent());
											if(NumberUtils.isNumber(id.getTextContent()) && id.getTagName().equalsIgnoreCase("_id")){
												replaceID = Integer.parseInt(id.getTextContent());
												obj = obj.append(id.getTagName(), Integer.parseInt(id.getTextContent()));
											}
											else if(NumberUtils.isNumber(id.getTextContent())){
												obj = obj.append(id.getTagName(), Integer.parseInt(id.getTextContent()));
											}
											else{
												obj = obj.append(id.getTagName(), id.getTextContent());
											}
										}
									}
								}
							}
							
							//So far we've got the new document that will replace the old document
							
							if(replaceID <= 0){
								response.sendError(404, "Cannot locate resource.");
							}
							else{
								BasicDBObject finderObject = new BasicDBObject();
								DBObject oldObject = new BasicDBObject();
								finderObject.put("_id", replaceID);
								
								DBCursor cursor = col.find(finderObject);
								
								while(cursor.hasNext()){
									oldObject=cursor.next();
								}
								
								col.update(oldObject, obj);
								
								pw.println(docType + 
									"<html>\n" +
									"<head><title>" + title + "</title></head>\n" +
									"<body>" +
									"<h1>Status 200: OK</h1>" +
									"</body>" +
									"</html>"
								);
								
							}
							
							
								
						} catch (ParserConfigurationException e1) {
							response.sendError(400, "Parser Configuration Exception: " + e1.toString());
							e1.printStackTrace();
						} catch (SAXException e1) {
							response.sendError(400, "SAX Exception: " + e1.toString());
							e1.printStackTrace();
						}
					}
					
					
					//DELETE METHOD
					else if(request.getParameter("method").equalsIgnoreCase("delete")){
						if(l==1){
							response.sendError(501, "Cannot delete entire table");
						}
						else if(l==2){
							int delID = 0;
							String id = objIDs[1];
							if(NumberUtils.isNumber(id)){
								delID = Integer.parseInt(id);
							}
							
							if(delID <= 0){
								response.sendError(404, "Resource not found");
							}
							else{
								BasicDBObject finderObject = new BasicDBObject();
								finderObject.put("_id", delID);
								
								DBCursor cursor = col.find(finderObject);
								
								if(cursor.hasNext()){
									col.remove(finderObject);
									
									pw.println(docType + 
										"<html>\n" +
										"<head><title>" + title + "</title></head>\n" +
										"<body>" +
										"<h1>Status: 200 OK</h1>" +
										"</body>" +
										"</html>"
									);
								}
								else{
									response.sendError(404, "Cannot locate resource");
								}
							}
						}
						else{
							response.sendError(501, "Number of arguments should be 1 or 2");
						}
					}
				}
				
				else{
					response.sendError(404, "Cannot find resource \"" + request.getParameter("url") + "\"");
				}
			}
			else{
				response.sendError(404, "Collection not found");
			}
			
			
		}
		else{
			response.sendError(404, "Document not found");
		}
		
	}
	
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	

}
