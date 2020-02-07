import com.me.pojo.Bill;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashSet;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:hibernate.cfg.xml","/WEB-INF/applicationContext.xml","/WEB-INF/dispatcher-servlet.xml"})
public class ControllerTest {

    private MockMvc mock;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Before
    public void before(){
        mock = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testCreateUser() throws Exception {
        String url = "/v1/user";
        JSONObject j = new JSONObject();

        //invalid
        j.put("password","Abcd1234.");
        j.put("last_name","ooo");
        j.put("first_name","ooo");
        j.put("email_address","123@sum.com");
        //other fields should be ignored
        j.put("account_created","anything");

        MvcResult mvcRes = mock.perform(MockMvcRequestBuilders.post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(j.toString().getBytes())
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void testCreateBill() throws Exception {
        String url = "/v1/bill/";

        Bill bill = new Bill();
        bill.setCategories(new HashSet<>());
        bill.setAmount_due(1000.01);
        bill.setBill_date(LocalDate.now());
        bill.setDue_date(LocalDate.now());
        bill.setVendor("TEST");
        bill.setPaymentStatus(Bill.status.paid);

        String auth = "wed@gmail.com:Abcd1234.";
        String base = new String(Base64.encode(auth.getBytes()));

        MvcResult mvcRes = mock.perform(MockMvcRequestBuilders.post(url)
                .header("Authorization","Basic " + base)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bill.toJSON().toString().getBytes())
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void testAuthorization() throws Exception {
        String url = "/v1/bill/";

        MvcResult mvcRes = mock.perform(MockMvcRequestBuilders.post(url)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void testGetBills() throws Exception {
        String url = "/v1/bills";

        String auth = "wed@gmail.com:Abcd1234.";
        String base = new String(Base64.encode(auth.getBytes()));

        MvcResult mvcRes = mock.perform(MockMvcRequestBuilders.get(url)
                .header("Authorization","Basic" + base)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    public void testGetUser() throws Exception {
        String url = "/v1/user/self";

        String auth = "wed@gmail.com:Abcd1234.";
        String base = new String(Base64.encode(auth.getBytes()));

        MvcResult mvcRes = mock.perform(MockMvcRequestBuilders.get(url)
                .header("Authorization","Basic" + base)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

}