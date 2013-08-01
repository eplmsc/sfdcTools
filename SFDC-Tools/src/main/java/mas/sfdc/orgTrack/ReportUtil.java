package mas.sfdc.orgTrack;
import java.util.Map;

import com.sforce.soap.metadata.FileProperties;


public class ReportUtil {
	public void outputChangeReport(
			Map<String, FileProperties[]> metadataPropertiesMap) {
		
		int maxWidthType = 0;
		int maxWidthName = 0;
		for (String elementName: metadataPropertiesMap.keySet()) {
			for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
				if (fp.getLastModifiedDate().after(this.afterDate)) {
					if (fp.getType().length() > maxWidthType) {
						maxWidthType = fp.getType().length(); 
					}
					if (fp.getFullName().length() > maxWidthName) {
						maxWidthName = fp.getFullName().length(); 
					}
				}
			}			
		}
		
		maxWidthName += 4;
		maxWidthType += 4;
		
		String formatString = "%-"+maxWidthType+"s %-"+maxWidthName+"s %-20s %s%n";
		
		for (String elementName: metadataPropertiesMap.keySet()) {
			int breakpoint=0;breakpoint=breakpoint;
			for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
				if (fp.getLastModifiedDate().after(this.afterDate)) {					
					System.out.format(formatString
							,fp.getType()
							,fp.getFullName()
							,outputDateFormat.format(fp.getLastModifiedDate().getTime())
							,fp.getLastModifiedByName());
				}
			}
		}
	}

	public void outputPackageFormat(
			Map<String, FileProperties[]> metadataPropertiesMap) {

		System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		System.out.println("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");
		
		for (String elementName: metadataPropertiesMap.keySet()) {
			Boolean openType = true;
			Boolean closeType = false;
			for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
				if (fp.getLastModifiedDate().after(this.afterDate)) {
					if (openType) {
						System.out.println("    <types>");
						openType = false;
						closeType = true;
					}
					System.out.println("        <members>"+fp.getFullName()+"</members>");
				}
			}
			if (closeType) {
				System.out.println("        <name>"+elementName+"</name>");
				System.out.println("    </types>");
			}
		}
		System.out.println("</Package>");
	}

}
