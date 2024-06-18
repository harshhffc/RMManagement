package models.notification;

import com.opencsv.bean.CsvBindByPosition;

public class NotificationCSV {

	@CsvBindByPosition(position = 0)
	public String rmSfId;
	
	@CsvBindByPosition(position = 1)
	public String title;
	
	@CsvBindByPosition(position = 2)
	public String message;
	
	@CsvBindByPosition(position = 3)
	public String webUrl;
}
