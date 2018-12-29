package com.alibaba.otter.canal.example.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class VbAddProduce {

    public static void sendMessage(String message) {

        if(message == null){
            message = "";
        }

        ////创建spring容器
        //ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        ////获得JmsTemplate对象
        //JmsTemplate template = (JmsTemplate) applicationContext.getBean("jmsTemplate");
        ////获得Destination
        //ActiveMQQueue queue = (ActiveMQQueue) applicationContext.getBean("queueDestination");
        ////发送消息
        //template.send(queue, new MessageCreator() {
        //
        //    @Override
        //    public Message createMessage(Session session) throws JMSException {
        //        return session.createTextMessage("topic：" + message);
        //    }
        //});

        //创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        try {
            //创建连接
            Connection connection = connectionFactory.createConnection();
            //开启连接
            connection.start();
            //创建一个回话
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //创建一个Destination，queue或者Topic
            Topic topic = session.createTopic("mytopic");
            //创建一个生成者
            MessageProducer producer = session.createProducer(topic);
            //创建一个消息
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("hello my topic：" + message);
            //发送消息
            producer.send(textMessage);
            //关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main (String[] args){
        sendMessage("@@@@@@@@@@@@@@");
    }
}
