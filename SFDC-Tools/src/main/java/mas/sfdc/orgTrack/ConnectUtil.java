package mas.sfdc.orgTrack;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.sforce.soap.metadata.AsyncRequestState;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.RetrieveMessage;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class ConnectUtil {

		public static final String SFDC_AUTHPATH = "/services/Soap/u/28.0";
		public static final double API_VERSION = 28.0;
		
	    private static final long ONE_SECOND = 1000;
	    private static final int MAX_NUM_POLL_REQUESTS = 50; 

		
		/**
		 * @param args
		 */
//		public static void main(String[] args) {
//			
//			Test01 self = new Test01();		
//			self.getCommandLine(args);
//			
//			MetadataConnection metadataConnection = self.connect();
//			Map<String, FileProperties[]> metadataPropertiesMap = self.getAllMetadataProperties(metadataConnection);
//			self.outputChangeReport(metadataPropertiesMap);
//			System.out.println();System.out.println();
//			self.outputPackageFormat(metadataPropertiesMap);
//			
//		}


		public Map<String, FileProperties[]> getAllMetadataProperties(MetadataConnection metadataConnection) {
			System.out.println("Getting metadata...");
			Map<String, FileProperties[]> result = new HashMap<String, FileProperties[]>();
			try {
				DescribeMetadataResult dmr = metadataConnection.describeMetadata(API_VERSION);		
				ListMetadataQuery[] queries = new ListMetadataQuery[1];
				for (int i=0; i<dmr.getMetadataObjects().length; i++) {								
					queries[0] = new ListMetadataQuery();
					queries[0].setType(dmr.getMetadataObjects()[i].getXmlName());

					FileProperties[] queryResult = metadataConnection.listMetadata(queries, API_VERSION);
					result.put(dmr.getMetadataObjects()[i].getXmlName(), queryResult);
					System.out.print(".");
				}
				System.out.println(".");
			} catch (ConnectionException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return result;
		}

		public MetadataConnection connect() {
			System.out.println("Logging in...");

			MetadataConnection result = null;

			String fullPassword = this.token != null ? this.password + this.token : this.password;
			
			ConnectorConfig partnerConfig = new ConnectorConfig();
			partnerConfig.setAuthEndpoint(this.authenticationUrl+SFDC_AUTHPATH);
			partnerConfig.setUsername(this.userId);
			partnerConfig.setPassword(fullPassword);		

			PartnerConnection partnerConnection;
			try {
				partnerConnection = new PartnerConnection(partnerConfig);
				LoginResult loginResult = partnerConnection.login(this.userId, fullPassword);

				ConnectorConfig metadataConfig = new ConnectorConfig();
				metadataConfig.setServiceEndpoint(loginResult.getMetadataServerUrl());
				metadataConfig.setSessionId(loginResult.getSessionId());
				
				result = new MetadataConnection(metadataConfig);
			} catch (ConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
				
			return result;
		}
		
		private void retrieveZip(MetadataConnection metadatabinding) throws RemoteException, Exception
	    {
	        RetrieveRequest retrieveRequest = new RetrieveRequest();
	        retrieveRequest.setApiVersion(API_VERSION);
	        setUnpackaged(retrieveRequest);

	        AsyncResult asyncResult = metadatabinding.retrieve(retrieveRequest);
	        // Wait for the retrieve to complete
	        int poll = 0;
	        long waitTimeMilliSecs = ONE_SECOND;
	        while (!asyncResult.isDone()) {
	            Thread.sleep(waitTimeMilliSecs);
	            // double the wait time for the next iteration
	            waitTimeMilliSecs *= 2;
	            if (poll++ > MAX_NUM_POLL_REQUESTS) {
	                throw new Exception("Request timed out.  If this is a large set " +
	                		"of metadata components, check that the time allowed " +
	                		"by MAX_NUM_POLL_REQUESTS is sufficient.");
	            }
	            asyncResult = metadatabinding.checkStatus(
	            		new String[] {asyncResult.getId()})[0];
	            System.out.println("Status is: " + asyncResult.getState());
	        }

	        if (asyncResult.getState() != AsyncRequestState.Completed) {
	            throw new Exception(asyncResult.getStatusCode() + " msg: " +
	                    asyncResult.getMessage());
	        }

	        RetrieveResult result = metadatabinding.checkRetrieveStatus(asyncResult.getId());
	        
	        // Print out any warning messages
	        StringBuilder buf = new StringBuilder();
	        if (result.getMessages() != null) {
	            for (RetrieveMessage rm : result.getMessages()) {
	                buf.append(rm.getFileName() + " - " + rm.getProblem());
	            }
	        }
	        if (buf.length() > 0) {
	            System.out.println("Retrieve warnings:\n" + buf);
	        }

	        // Write the zip to the file system
	        System.out.println("Writing results to zip file");
	        ByteArrayInputStream bais = new ByteArrayInputStream(result.getZipFile());
	        File resultsFile = new File("retrieveResults.zip");
	        FileOutputStream os = new FileOutputStream(resultsFile);
	        try {
	            ReadableByteChannel src = Channels.newChannel(bais);
	            FileChannel dest = os.getChannel();
	            copy(src, dest);
	            
	            System.out.println("Results written to " + resultsFile.getAbsolutePath());
	        }
	        finally {
	            os.close();
	        }

	    }
	    
	    private void setUnpackaged(RetrieveRequest request) throws Exception
	    {
	        // Edit the path, if necessary, if your package.xml file is located elsewhere
	        File unpackedManifest = new File(MANIFEST_FILE);
	        System.out.println("Manifest file: " + unpackedManifest.getAbsolutePath());
	        
	        if (!unpackedManifest.exists() || !unpackedManifest.isFile())
	            throw new Exception("Should provide a valid retrieve manifest " +
	                    "for unpackaged content. " +
	                    "Looking for " + unpackedManifest.getAbsolutePath());

	        // Note that we populate the _package object by parsing a manifest file here.
	        // You could populate the _package based on any source for your
	        // particular application.
	        _package p = parsePackage(unpackedManifest);
	        request.setUnpackaged(p);
	    }
	 
	    
	    private _package parsePackage(File file) throws Exception {
	        try {
	            InputStream is = new FileInputStream(file);
	            List<PackageTypeMembers> pd = new ArrayList<PackageTypeMembers>();
	            DocumentBuilder db =
	                DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            Element d = db.parse(is).getDocumentElement();
	            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
	                if (c instanceof Element) {
	                    Element ce = (Element)c;
	                    //
	                    NodeList namee = ce.getElementsByTagName("name");
	                    if (namee.getLength() == 0) {
	                        // not
	                        continue;
	                    }
	                    String name = namee.item(0).getTextContent();
	                    NodeList m = ce.getElementsByTagName("members");
	                    List<String> members = new ArrayList<String>();
	                    for (int i = 0; i < m.getLength(); i++) {
	                        Node mm = m.item(i);
	                        members.add(mm.getTextContent());
	                    }
	                    PackageTypeMembers pdi = new PackageTypeMembers();
	                    pdi.setName(name);
	                    pdi.setMembers(members.toArray(new String[members.size()]));
	                    pd.add(pdi);
	                }
	            }
	            _package r = new _package();
	            r.setTypes(pd.toArray(new PackageTypeMembers[pd.size()]));
	            r.setVersion(API_VERSION + "");
	            return r;
	        } catch (ParserConfigurationException pce) {
	            throw new Exception("Cannot create XML parser", pce);
	        } catch (IOException ioe) {
	            throw new Exception(ioe);
	        } catch (SAXException se) {
	            throw new Exception(se);
	        }
	    }

}
