package mas.sfdc.orgTrack;

import mas.sfdc.stuff.CliFacade;


public class GetPackageXmlCli {
	public static void main(String[] args) {
		CliFacade cliFacade = new CliFacade();
		cliFacade.getCommandLine(args);
		
		System.out.println(cliFacade.getUsername());
		System.out.println(cliFacade.getPassword());
		System.out.println(cliFacade.getServerurl());
		System.out.println(cliFacade.getFilename());
		System.out.println(cliFacade.getApiVersion());
		
	}
}
