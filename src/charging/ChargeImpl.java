package charging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.DBConnection;

//api servicekey�� �̿��ؼ� ���������ͼ������� ������ �������� parsing�ؿͼ� DB�� ����ϴ� Ŭ����
public class ChargeImpl implements ChargeDAO
{
	Connection conn;
	DBConnection connDB = new DBConnection();
	PreparedStatement pstmt;
	ResultSet rs;
	String serviceKey ="����Ű�� �Է��ϼ���";
	public void refresh()
	{
		conn = connDB.tryConnection();
		try
		{
			PreparedStatement pstmt = conn.prepareStatement("TRUNCATE TABLE search");
			rs = pstmt.executeQuery();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			PreparedStatement pstmt=conn.prepareStatement("Insert into search values(?,?,?,?,?,?,?,?)");
			String url = "http://apis.data.go.kr/B552584/EvCharger/getChargerInfo?serviceKey="+serviceKey+"&pageNo="+1+"&numOfRows=4000&zcode=30";
			
			DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
			Document doc = dBuilder.parse(url);
			
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("item");
			
			for(int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					
					String statNm = this.getTagValue("statNm", eElement);		//�����Ҹ�
					String statid = this.getTagValue("statId", eElement);		//������ID
					int chgerid = Integer.parseInt(this.getTagValue("chgerId", eElement));		//������ID
					String chagerType = this.getTagValue("chgerType", eElement);		//������Ÿ��
					
					//������Ÿ���� ���ڷ� �������ֱ� ������ Stringtype���� ��ȯ �� DB�� ���
					switch (chagerType)
					{
	                    case "01" : chagerType = "DC������";
	                    break;
	                    case "02" : chagerType = "AC�ϼ�";
	                    break;
	                    case "03" : chagerType = "DC������+AC3��";
	                    break;
	                    case "04" : chagerType = "DC�޺�";
	                    break;
	                    case "05" : chagerType = "DC������+DC�޺�";
	                    break;
	                    case "06" : chagerType = "DC������+AC3��+DC�޺�";
	                    break;
	                    case "07" : chagerType = "AC3��";
	                    break;
					}
					
					String addr = this.getTagValue("addr", eElement);		//�ּ�
					String location  = this.getTagValue("location", eElement);	//����ġ
					String stat = this.getTagValue("stat", eElement);			//������ ����(1: ����̻�, 2: �������, 3: ������, 4: �����, 5: ������, 9: ���¹�Ȯ��)
					
					//������ ���������� Stringtype���� DB�� ���
					switch (stat)
					{
	                    case "1" : stat = "����̻�";
	                    break;
	                    case "2" : stat = "�������";
	                    break;
	                    case "3" : stat = "������";
	                    break;
	                    case "4" : stat = "�����";
	                    break;
	                    case "5" : stat = "������";
	                    break;
	                    case "9" : stat = "���¹�Ȯ��";
	                    break;
					}
					Long statUpdDt = Long.parseLong(this.getTagValue("statUpdDt", eElement));		//������ ���� ���� �Ͻ�
					Long lastTsdt = Long.parseLong(this.getTagValue("lastTsdt", eElement));		//������ ���� �����Ͻ�
					Long lastTedt = Long.parseLong(this.getTagValue("lastTedt", eElement));		//������ ���������Ͻ�
					Long nowTsdt = Long.parseLong(this.getTagValue("nowTsdt", eElement));		//������ �����Ͻ�
					double lat = Double.parseDouble(this.getTagValue("lat", eElement));
					double lng = Double.parseDouble(this.getTagValue("lng", eElement));
					
					pstmt.setString(1, statNm);
					pstmt.setString(2, statid);
					pstmt.setInt(3, chgerid);
					pstmt.setString(4, chagerType);
					pstmt.setString(5, stat);
					pstmt.setString(6, addr);
					pstmt.setDouble(7, lat);
					pstmt.setDouble(8, lng);
					
					pstmt.addBatch();
					pstmt.clearParameters();
				}	// if end
				pstmt.executeBatch();
				pstmt.clearBatch();
			}	// for end
			System.out.println("�߰� �Ϸ�");
		}
		
		catch (Exception e)
		{	
			e.printStackTrace();
		}	// try~catch end
		finally
		{
			connDB.close(conn, pstmt, rs);
		}
	}	// Paser end
	
		// tag���� ������ �������� �޼ҵ�
		public String getTagValue(String tag, Element eElement)
		{
		    NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
		    Node nValue = (Node) nlList.item(0);
		    if(nValue == null) 
		        return "0";
		    return nValue.getNodeValue();	
		}

}
