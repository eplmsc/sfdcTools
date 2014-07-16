package mas.sfdc.stuff;



public class Example {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Example self = new Example();
		self.testIfItWorks(args);

	}

	public void testIfItWorks(String[] commandLine) {
		CliFacade cliFacade = new CliFacade();
		cliFacade.getCommandLine(commandLine);
		
		System.out.println("userId="+cliFacade.getUserId());
		System.out.println("password="+cliFacade.getPassword());
		System.out.println("token="+cliFacade.getToken());
		System.out.println("authenticationUrl="+cliFacade.getAuthenticationUrl());
		System.out.println("packageOutput="+cliFacade.isPackageOutput());
		System.out.println("beforeDate="+cliFacade.getBeforeDate());
		System.out.println("afterDate="+cliFacade.getAfterDate());
		
	}
}
