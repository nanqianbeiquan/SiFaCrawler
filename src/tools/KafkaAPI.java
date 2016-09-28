package tools;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaAPI {

	private Producer<String,String> producer;
	public KafkaAPI() throws IOException  
	{
		Properties properties = new Properties();
		System.out.println(new File("conf/kafkaProducer.properties").exists());
		properties.load(ClassLoader.getSystemResourceAsStream("kafkaProducer.properties"));
		ProducerConfig config = new ProducerConfig(properties);
		producer = new Producer<String, String>(config);
	}


	public void send(String topic,String message) 
	{
		producer.send(new KeyedMessage<String, String>(topic, message));
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		KafkaAPI kafaka=new KafkaAPI();
		for(int i=1;i<10;i++)
		{
			kafaka.send("SifaCrawlerTest", "test"+i);
			System.out.println("test"+i);
			TimeUnit.SECONDS.sleep(1);
		}
	}

}