package mas.sfdc.orgTrack;

import java.io.PrintStream;
import java.util.Map;

import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;

public class ReportUtil {
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
}
