package mas.sfdc.orgTrack;

import java.io.PrintStream;
import java.util.Map;

import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;

public class ReportUtil {
	// public void outputChangeReport(
	// Map<String, FileProperties[]> metadataPropertiesMap) {
	//
	// int maxWidthType = 0;
	// int maxWidthName = 0;
	// for (String elementName: metadataPropertiesMap.keySet()) {
	// for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
	// if (fp.getLastModifiedDate().after(this.afterDate)) {
	// if (fp.getType().length() > maxWidthType) {
	// maxWidthType = fp.getType().length();
	// }
	// if (fp.getFullName().length() > maxWidthName) {
	// maxWidthName = fp.getFullName().length();
	// }
	// }
	// }
	// }
	//
	// maxWidthName += 4;
	// maxWidthType += 4;
	//
	// String formatString =
	// "%-"+maxWidthType+"s %-"+maxWidthName+"s %-20s %s%n";
	//
	// for (String elementName: metadataPropertiesMap.keySet()) {
	// int breakpoint=0;breakpoint=breakpoint;
	// for (FileProperties fp: metadataPropertiesMap.get(elementName)) {
	// if (fp.getLastModifiedDate().after(this.afterDate)) {
	// System.out.format(formatString
	// ,fp.getType()
	// ,fp.getFullName()
	// ,outputDateFormat.format(fp.getLastModifiedDate().getTime())
	// ,fp.getLastModifiedByName());
	// }
	// }
	// }
	// }

	/**
	 * @description Writes a qualified package.xml (ie. including element names) to a given PrintStream
	 * @param metadataPropertiesMap Map of elementName -> FileProperties
	 * @param out PrintStream used for emitting the output
	 */
	public static void outputQualifiedPackageFormat(
			Map<String, FileProperties[]> metadataPropertiesMap, PrintStream out) {

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");

		for (String elementName : metadataPropertiesMap.keySet()) {
			Boolean openType = true;
			Boolean closeType = false;
			for (FileProperties fp : metadataPropertiesMap.get(elementName)) {
				if (openType) {
					out.println("    <types>");
					openType = false;
					closeType = true;
				}
				out.println("        <members>" + fp.getFullName()
						+ "</members>");
			}
			if (closeType) {
				out.println("        <name>" + elementName + "</name>");
				out.println("    </types>");
			}
		}
		out.println("</Package>");
	}

	/**
	 * @description Writes a non-qualified package.xml (ie. using * rather than element names) to a given PrintStream
	 * @param metadataPropertiesMap Map of elementName -> FileProperties
	 * @param out PrintStream used for emitting the output
	 */
	public static void outputNonSpecificPackageFormat(
			DescribeMetadataResult dmr, PrintStream out) {

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");

		for (int i = 0; i < dmr.getMetadataObjects().length; i++) {
			out.println("    <types>");
			out.println("        <members>*</members>");
			out.println("        <name>"
					+ dmr.getMetadataObjects()[i].getXmlName() + "</name>");
			out.println("    </types>");
		}
		out.println("</Package>");
	}

	// public void describeMetadata() {
	// try {
	// double apiVersion = 21.0;
	// // Assuming that the SOAP binding has already been established.
	// DescribeMetadataResult res =
	// metadataConnection.describeMetadata(apiVersion);
	// StringBuffer sb = new StringBuffer();
	// if (res != null && res.getMetadataObjects().length > 0) {
	// for (DescribeMetadataObject obj : res.getMetadataObjects()) {
	// sb.append("***************************************************\n");
	// sb.append("XMLName: " + obj.getXmlName() + "\n");
	// sb.append("DirName: " + obj.getDirectoryName() + "\n");
	// sb.append("Suffix: " + obj.getSuffix() + "\n");
	// sb.append("***************************************************\n");
	// }
	// } else {
	// sb.append("Failed to obtain metadata types.");
	// }
	// System.out.println(sb.toString());
	// } catch (ConnectionException ce) {
	// ce.printStackTrace();
	// }
	// }
}
