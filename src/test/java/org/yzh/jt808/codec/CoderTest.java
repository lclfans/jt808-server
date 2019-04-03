package org.yzh.jt808.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.yzh.framework.commons.transform.JsonUtils;
import org.yzh.framework.message.PackageData;
import org.yzh.web.config.Charsets;
import org.yzh.web.jt808.codec.JT808MessageDecoder;
import org.yzh.web.jt808.codec.JT808MessageEncoder;
import org.yzh.web.jt808.dto.*;
import org.yzh.web.jt808.dto.basics.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * JT/T 808协议单元测试类
 *
 * @author zhihao.ye (yezhihaoo@gmail.com)
 */
public class CoderTest {

    private static final JT808MessageDecoder decoder = new JT808MessageDecoder(Charsets.GBK);

    private static final JT808MessageEncoder encoder = new JT808MessageEncoder(Charsets.GBK);

    public static <T extends PackageData> T transform(Class<T> clazz, String hex) {
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        Header header = decoder.decodeHeader(buf);
        ByteBuf slice = buf.slice(header.getHeaderLength(), header.getBodyLength());
        PackageData<Header> body = decoder.decodeBody(slice, clazz);
        body.setHeader(header);
        return (T) body;
    }

    public static String transform(PackageData<Header> packageData) {
        ByteBuf buf = encoder.encodeAll(packageData);
        String hex = ByteBufUtil.hexDump(buf);
        return hex;
    }

    public static void selfCheck(Class<? extends PackageData> clazz, String hex1) {
        PackageData bean1 = transform(clazz, hex1);

        String hex2 = transform(bean1);
        PackageData bean2 = transform(clazz, hex2);

        System.out.println(hex1);
        System.out.println(hex2);
        System.out.println(JsonUtils.toJson(bean1));
        System.out.println(JsonUtils.toJson(bean2));
        System.out.println();

        assert hex1.equals(hex2);
        assert JsonUtils.toJson(bean1).equals(JsonUtils.toJson(bean2));
    }

    public static void selfCheck(PackageData<Header> bean1) {
        String hex1 = transform(bean1);

        PackageData bean2 = transform(bean1.getClass(), hex1);
        String hex2 = transform(bean2);

        System.out.println(hex1);
        System.out.println(hex2);
        System.out.println(JsonUtils.toJson(bean1));
        System.out.println(JsonUtils.toJson(bean2));
        System.out.println();

        assert hex1.equals(hex2);
        assert JsonUtils.toJson(bean1).equals(JsonUtils.toJson(bean2));
    }

    public static Header header() {
        Header header = new Header();
        header.setType(1);
        header.setMobileNumber("020000000015");
        header.setSerialNumber(37);
        header.setEncryptionType(0);
        header.setReservedBit(0);
        return header;
    }


    // 位置信息汇报 0x0200
    @Test
    public void testPositionReport() {
        String hex1 = "0200006a064762924976014d000003500004100201d9f1230743425e000300a6ffff190403133450000000250400070008000000e2403836373733323033383535333838392d627566322d323031392d30342d30332d31332d33342d34392d3735372d70686f6e652d2e6a706700000020000c14cde78d";
        selfCheck(PositionReport.class, hex1);
    }


    // 终端注册 0x0100
    @Test
    public void testRegister() {
        PackageData bean1 = register();
        selfCheck(bean1);
    }

    public static PackageData<Header> register() {
        Register b = new Register();
        b.setHeader(header());
        b.setProvinceId(44);
        b.setCityId(307);
        b.setManufacturerId("测试");
        b.setTerminalType("TEST");
        b.setTerminalId("粤B8888");
        b.setLicensePlateColor(0);
        b.setLicensePlate("粤B8888");
        return b;
    }


    // 提问下发 0x8302
    @Test
    public void testQuestionMessage() {
        selfCheck(QuestionMessage.class, "8302001a017701840207001010062c2c2c2c2c2101000331323302000334353603000337383954");

        selfCheck(questionMessage());
    }

    public static PackageData<Header> questionMessage() {
        QuestionMessage bean = new QuestionMessage();
        List<QuestionMessage.Option> options = new ArrayList();
        bean.setHeader(header());

        bean.buildSign(new int[]{1});
        bean.setContent("123");
        bean.setOptions(options);

        options.add(new QuestionMessage.Option(1, "asd1"));
        options.add(new QuestionMessage.Option(2, "zxc2"));
        return bean;
    }


    // 设置电话本 0x8401
    @Test
    public void testPhoneBook() {
        selfCheck(PhoneBook.class, "0001002e02000000001500250203020b043138323137333431383032d5c5c8fd010604313233313233c0eecbc4030604313233313233cdf5cee535");

        selfCheck(phoneBook());
    }

    public static PhoneBook phoneBook() {
        PhoneBook bean = new PhoneBook();
        bean.setHeader(header());
        bean.setType(PhoneBook.Append);
        bean.add(new PhoneBook.Item(2, "18217341802", "张三"));
        bean.add(new PhoneBook.Item(1, "123123", "李四"));
        bean.add(new PhoneBook.Item(3, "123123", "王五"));
        return bean;
    }


    // 事件设置 0x8301
    @Test
    public void testEventSetting() {
        selfCheck(EventSetting.class, "83010010017701840207000c0202010574657374310205746573743268");

        selfCheck(eventSetting());
    }

    public static EventSetting eventSetting() {
        EventSetting bean = new EventSetting();
        bean.setHeader(header());
        bean.setType(EventSetting.Append);
        bean.addEvent(1, "test");
        bean.addEvent(2, "测试2");
        bean.addEvent(3, "t试2");
        return bean;
    }


    // 终端&平台通用应答 0x0001 0x8001
    @Test
    public void testCommonResult() {
        selfCheck(CommonResult.class, "0001000501770184020701840038810300cd");
    }
}