package mas.sfdc.orgTrack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class CopiedFromDocumentation {

//    // binding for the Enterprise WSDL used for login() call
//	private SoapBindingStub binding;
//    // binding for the metadata WSDL used for create() and checkStatus() calls
//	private MetadataBindingStub metadatabinding;
//
//	static BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
//
//    // one second in milliseconds
//    private static final long ONE_SECOND = 1000;
//    // maximum number of attempts to retrieve the results
//    private static final int MAX_NUM_POLL_REQUESTS = 50; 
//
//    // manifest file that controls which components get retrieved
//    private static final String MANIFEST_FILE = "package.xml"; 
//
//    private static final double API_VERSION = 15.0; 
//
//    public static void main(String[] args) throws ServiceException, Exception {
//        RetrieveSample sample = new RetrieveSample();
//        sample.run();
//    }
//
//    private void run() throws ServiceException, Exception {
//        if (login()) {
//            getUserInput("SUCCESSFUL LOGIN! Hit the enter key to continue.");
//            retrieveZip();
//        }
//    }
//
//    
//    private void retrieveZip() throws RemoteException, Exception
//    {
//        RetrieveRequest retrieveRequest = new RetrieveRequest();
//        retrieveRequest.setApiVersion(API_VERSION);
//        setUnpackaged(retrieveRequest);
//
//        AsyncResult asyncResult = metadatabinding.retrieve(retrieveRequest);
//        // Wait for the retrieve to complete
//        int poll = 0;
//        long waitTimeMilliSecs = ONE_SECOND;
//        while (!asyncResult.isDone()) {
//            Thread.sleep(waitTimeMilliSecs);
//            // double the wait time for the next iteration
//            waitTimeMilliSecs *= 2;
//            if (poll++ > MAX_NUM_POLL_REQUESTS) {
//                throw new Exception("Request timed out.  If this is a large set " +
//                		"of metadata components, check that the time allowed " +
//                		"by MAX_NUM_POLL_REQUESTS is sufficient.");
//            }
//            asyncResult = metadatabinding.checkStatus(
//            		new String[] {asyncResult.getId()})[0];
//            System.out.println("Status is: " + asyncResult.getState());
//        }
//
//        if (asyncResult.getState() != AsyncRequestState.Completed) {
//            throw new Exception(asyncResult.getStatusCode() + " msg: " +
//                    asyncResult.getMessage());
//        }
//
//        RetrieveResult result = metadatabinding.checkRetrieveStatus(asyncResult.getId());
//        
//        // Print out any warning messages
//        StringBuilder buf = new StringBuilder();
//        if (result.getMessages() != null) {
//            for (RetrieveMessage rm : result.getMessages()) {
//                buf.append(rm.getFileName() + " - " + rm.getProblem());
//            }
//        }
//        if (buf.length() > 0) {
//            System.out.println("Retrieve warnings:\n" + buf);
//        }
//
//        // Write the zip to the file system
//        System.out.println("Writing results to zip file");
//        ByteArrayInputStream bais = new ByteArrayInputStream(result.getZipFile());
//        File resultsFile = new File("retrieveResults.zip");
//        FileOutputStream os = new FileOutputStream(resultsFile);
//        try {
//            ReadableByteChannel src = Channels.newChannel(bais);
//            FileChannel dest = os.getChannel();
//            copy(src, dest);
//            
//            System.out.println("Results written to " + resultsFile.getAbsolutePath());
//        }
//        finally {
//            os.close();
//        }
//
//    }
//    
//    /**
//     * Helper method to copy from a readable channel to a writable channel,
//     * using an in-memory buffer.
//     */
//    private void copy(ReadableByteChannel src, WritableByteChannel dest)
//        throws IOException
//    {
//        // use an in-memory byte buffer
//        ByteBuffer buffer = ByteBuffer.allocate(8092);
//        while (src.read(buffer) != -1) {
//            buffer.flip();
//            while(buffer.hasRemaining()) {
//                dest.write(buffer);
//            }
//            buffer.clear();
//        }
//    }
//    
//    private void setUnpackaged(RetrieveRequest request) throws Exception
//    {
//        // Edit the path, if necessary, if your package.xml file is located elsewhere
//        File unpackedManifest = new File(MANIFEST_FILE);
//        System.out.println("Manifest file: " + unpackedManifest.getAbsolutePath());
//        
//        if (!unpackedManifest.exists() || !unpackedManifest.isFile())
//            throw new Exception("Should provide a valid retrieve manifest " +
//                    "for unpackaged content. " +
//                    "Looking for " + unpackedManifest.getAbsolutePath());
//
//        // Note that we populate the _package object by parsing a manifest file here.
//        // You could populate the _package based on any source for your
//        // particular application.
//        _package p = parsePackage(unpackedManifest);
//        request.setUnpackaged(p);
//    }
// 
//    private _package parsePackage(File file) throws Exception {
//        try {
//            InputStream is = new FileInputStream(file);
//            List<PackageTypeMembers> pd = new ArrayList<PackageTypeMembers>();
//            DocumentBuilder db =
//                DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Element d = db.parse(is).getDocumentElement();
//            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
//                if (c instanceof Element) {
//                    Element ce = (Element)c;
//                    //
//                    NodeList namee = ce.getElementsByTagName("name");
//                    if (namee.getLength() == 0) {
//                        // not
//                        continue;
//                    }
//                    String name = namee.item(0).getTextContent();
//                    NodeList m = ce.getElementsByTagName("members");
//                    List<String> members = new ArrayList<String>();
//                    for (int i = 0; i < m.getLength(); i++) {
//                        Node mm = m.item(i);
//                        members.add(mm.getTextContent());
//                    }
//                    PackageTypeMembers pdi = new PackageTypeMembers();
//                    pdi.setName(name);
//                    pdi.setMembers(members.toArray(new String[members.size()]));
//                    pd.add(pdi);
//                }
//            }
//            _package r = new _package();
//            r.setTypes(pd.toArray(new PackageTypeMembers[pd.size()]));
//            r.setVersion(API_VERSION + "");
//            return r;
//        } catch (ParserConfigurationException pce) {
//            throw new Exception("Cannot create XML parser", pce);
//        } catch (IOException ioe) {
//            throw new Exception(ioe);
//        } catch (SAXException se) {
//            throw new Exception(se);
//        }
//    }
//    
//    /**
//     * The login call is used to obtain a token from Salesforce.
//     * This token must be passed to all other calls to provide
//     * authentication.
//     */
//    private boolean login() throws ServiceException {
//        String userName = getUserInput("Enter username: ");
//        String password = getUserInput("Enter password: ");
//        /** Next, the sample client application initializes the binding stub.
//         * 
//         * This is our main interface to the API for the Enterprise WSDL.
//         * The getSoap method takes an optional parameter,
//         * (a java.net.URL) which is the endpoint.
//         * For the login call, the parameter always starts with 
//         * http(s)://login.salesforce.com. After logging in, the sample 
//         * client application changes the endpoint to the one specified 
//         * in the returned loginResult object.
//         */
//        binding = (SoapBindingStub) new SforceServiceLocator().getSoap();
//        
//        // Time out after a minute
//        binding.setTimeout(60000);
//        // Log in using the Enterprise WSDL binding
//        LoginResult loginResult;
//        try {
//            System.out.println("LOGGING IN NOW....");
//            loginResult = binding.login(userName, password);
//        }
//        catch (LoginFault ex) {
//  
//            // The LoginFault derives from AxisFault
//            ExceptionCode exCode = ex.getExceptionCode();
//            if (exCode == ExceptionCode.FUNCTIONALITY_NOT_ENABLED ||
//                exCode == ExceptionCode.INVALID_CLIENT ||
//                exCode == ExceptionCode.INVALID_LOGIN ||
//                exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_DOMAIN ||
//                exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_TIME ||
//                exCode == ExceptionCode.ORG_LOCKED ||
//                exCode == ExceptionCode.PASSWORD_LOCKOUT ||
//                exCode == ExceptionCode.SERVER_UNAVAILABLE ||
//                exCode == ExceptionCode.TRIAL_EXPIRED ||
//                exCode == ExceptionCode.UNSUPPORTED_CLIENT) {
//                System.out.println("Please be sure that you have a valid username " +
//                        "and password.");
//            } else {
//                // Write the fault code to the console
//                System.out.println(ex.getExceptionCode());
//                // Write the fault message to the console
//                System.out.println("An unexpected error has occurred." + ex.getMessage());
//            }
//            return false;
//        } catch (Exception ex) {
//            System.out.println("An unexpected error has occurred: " + ex.getMessage());
//            ex.printStackTrace();
//            return false;
//        }
//        // Check if the password has expired
//        if (loginResult.isPasswordExpired()) {
//            System.out.println("An error has occurred. Your password has expired.");
//            return false;
//        }
//        
//        /** Once the client application has logged in successfully, we use 
//         *  the results of the login call to reset the endpoint of the service  
//         *  to the virtual server instance that is servicing your organization.  
//         *  To do this, the client application sets the ENDPOINT_ADDRESS_PROPERTY 
//         *  of the binding object using the URL returned from the LoginResult. We
//         *  use the metadata binding from this point forward as we are invoking
//         *  calls in the metadata WSDL.
//         */
//        metadatabinding = (MetadataBindingStub)
//                new MetadataServiceLocator().getMetadata();
//        metadatabinding._setProperty(MetadataBindingStub.ENDPOINT_ADDRESS_PROPERTY,
//                loginResult.getMetadataServerUrl());
//
//        /** The sample client application now has an instance of the MetadataBindingStub 
//         *  that is pointing to the correct endpoint. Next, the sample client application 
//         *  sets a persistent SOAP header (to be included on all subsequent calls that 
//         *  are made with the SoapBindingStub) that contains the valid sessionId
//         *  for our login credentials. To do this, the sample client application 
//         *  creates a new SessionHeader object and set its sessionId property to the 
//         *  sessionId property from the LoginResult object.
//         */
//        // Create a new session header object and add the session id
//        // from the login return object
//        SessionHeader sh = new SessionHeader();
//        sh.setSessionId(loginResult.getSessionId());
//        /** Next, the sample client application calls the setHeader method of the 
//         *  SoapBindingStub to add the header to all subsequent method calls. This  
//         *  header will persist until the binding is destroyed or until the header 
//         *  is explicitly removed. The "SessionHeader" parameter is the name of the 
//         *  header to be added.
//         */
//        // set the session header for subsequent call authentication
//        metadatabinding.setHeader(
//            new MetadataServiceLocator().getServiceName().getNamespaceURI(),
//                "SessionHeader", sh);
//        
//        // return true to indicate that we are logged in, pointed
//        // at the right url and have our security token in place.
//        return true;
//    }
//    
//    //The sample client application retrieves the user's login credentials.
//    // Helper function for retrieving user input from the console
//    String getUserInput(String prompt) {
//        System.out.print(prompt);
//        try {
//            return rdr.readLine();
//        }
//        catch (IOException ex) {
//            return null;
//        }
//    }
//
}
