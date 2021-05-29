package com.github.athingx.athing.aliyun.qatest.framework.util;

import com.github.athingx.athing.aliyun.framework.util.GsonFactory;
import com.github.athingx.athing.aliyun.framework.util.MapObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;

public class GsonFactoryTestCase {

    private final Gson gson = GsonFactory.getGson();

    public enum Sex {
        BOY,
        GIRL
    }

    private final MapObject obj = new MapObject()
            .putProperty("age", 35)
            .putProperty("name", "vlinux")

            // contact
            .enterProperty("contact")
            .putProperty("email", "oldmanpushcart@gmail.com")
            .putProperty("github", "https://github.com/oldmanpushcart")
            .exitProperty()

            // special
            .enterProperty("special")
            .putProperty("date_type", new Date(12345))
            .putProperty("long_type", 1234567890L)
            .putProperty("boolean_type_true", true)
            .putProperty("boolean_type_false", false)
            .putProperty("enum_type_sex_boy", Sex.BOY)
            .putProperty("enum_type_sex_girl", Sex.GIRL)
            .exitProperty();


    private static void assertJson(String json, String xpath, String expect) {

        JsonElement curr = new JsonParser().parse(json);
        for (final String path : xpath.split("/")) {
            if ("".equals(path)) {
                continue;
            }
            curr = curr.getAsJsonObject().get(path);
        }

        Assert.assertThat(xpath, curr.toString(), equalTo(expect));

    }

    @Test
    public void test$to_json() {

        final String json = gson.toJson(obj);

        assertJson(json, "/age", "35");
        assertJson(json, "/name", "\"vlinux\"");

        assertJson(json, "/contact/email", "\"oldmanpushcart@gmail.com\"");
        assertJson(json, "/contact/github", "\"https://github.com/oldmanpushcart\"");

        assertJson(json, "/special/long_type", "\"1234567890\"");
        assertJson(json, "/special/date_type", "" + new Date(12345).getTime());
        assertJson(json, "/special/boolean_type_true", "1");
        assertJson(json, "/special/boolean_type_false", "0");
        assertJson(json, "/special/enum_type_sex_boy", "0");
        assertJson(json, "/special/enum_type_sex_girl", "1");

    }


    public static class Person {

        private String name;
        private int age;
        private Date birthday;
        private long telephone;
        private Sex sex;
        private boolean alive;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Person)) {
                return false;
            }
            Person person = (Person) o;
            return age == person.age
                    && telephone == person.telephone
                    && alive == person.alive
                    && Objects.equals(name, person.name)
                    && Objects.equals(birthday, person.birthday)
                    && sex == person.sex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, birthday, telephone, sex, alive);
        }
    }


    @Test
    public void test$from_json() {

        final Person person = new Person();
        person.name = "name";
        person.age = 35;
        person.birthday = new Date();
        person.telephone = 1234567890L;
        person.sex = Sex.GIRL;
        person.alive = false;

        final Person clone = gson.fromJson(gson.toJson(person), Person.class);
        Assert.assertEquals(person, clone);

    }

}
