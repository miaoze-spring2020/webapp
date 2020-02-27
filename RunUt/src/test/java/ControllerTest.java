import com.me.App;
import com.me.pojo.Bill;
import com.me.pojo.User;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = App.class)
//@AutoConfigureMockMvc
//@ContextConfiguration(locations = {"classpath:./hibernate.cfg.xml"})
public class ControllerTest {

    @Autowired
    private MockMvc mock;

    private Bill bill;

    private User user;

    @Test
    public void test(){
        return;
    }
//    @Before
//    public void before() throws Exception {
//        String url = "/v1/user";
//        user = new User();
//        user.setEmail_address("test@sun.com");
//        user.setPassword("Abcd1234.");
//        user.setLast_name("test");
//        user.setFirst_name("test");
//
//        JSONObject j = user.toJSON();
//        j.put("password",user.getPassword());
//
//        this.mock.perform(post(url)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(j.toString().getBytes())
//                .accept(MediaType.APPLICATION_JSON))
//                .andReturn();
//
//        String burl = "/v1/bill/";
//
//        bill = new Bill();
//        bill.setCategories(new HashSet<>());
//        bill.setAmount_due(1000.01);
//        bill.setBill_date(LocalDate.now());
//        bill.setDue_date(LocalDate.now());
//        bill.setVendor("TEST");
//        bill.setPaymentStatus(Bill.status.paid);
//        bill.setOwner(user);
//
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(post(burl).header("Authorization", "Basic " + base)).andReturn();
//    }
//
//    @Test
//    public void testCreateUser() throws Exception {
//        String url = "/v1/user";
//
//        mock.perform(post(url)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(user.toJSON().toString().getBytes())
//                .accept(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isBadRequest())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    @Rollback
//    public void testCreateBill() throws Exception {
//        String url = "/v1/bill/";
//
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        MvcResult mvcRes = mock.perform(post(url)
//                .header("Authorization", "Basic " + base)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(bill.toJSON().toString().getBytes())
//        )
//                .andExpect(status().isCreated())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    public void testAuthorization() throws Exception {
//        String url = "/v1/bills";
//
//        mock.perform(get(url))
//                .andExpect(status().isUnauthorized())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    public void testGetBills() throws Exception {
//        String url = "/v1/bills";
//
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(get(url)
//                .header("Authorization", "Basic" + base)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isOk())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    public void testGetUser() throws Exception {
//        String url = "/v1/user/self";
//
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(MockMvcRequestBuilders.get(url)
//                .header("Authorization", "Basic" + base)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isOk())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    @Rollback
//    public void testPutBill() throws Exception {
//        String url = "/v1/bill/123";
//
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(MockMvcRequestBuilders.put(url)
//                .header("Authorization", "Basic " + base)
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(bill.toJSON().toString().getBytes())
//        )
//                .andExpect(status().isNotFound())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//    }
//
//    @Test
//    @Rollback
//    public void testCreateFile() throws Exception {
//        String url = "/v1/bill/"+bill.getId()+"/file";
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(post(url)
//                .header("Authorization", "Basic" + base)
//        )
//                .andExpect(status().isNotFound())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//
//    }
//
//    @Test
//    @Rollback
//    public void testgetFile() throws Exception {
//        String url = "/v1/bill/"+bill.getId()+"/file/test";
//        String auth = user.getEmail_address() + ":" + user.getPassword();
//        String base = new String(Base64.encode(auth.getBytes()));
//
//        mock.perform(get(url)
//                .header("Authorization", "Basic" + base)
//        )
//                .andExpect(status().isNotFound())
//                .andDo(MockMvcResultHandlers.print())
//                .andReturn();
//
//    }

}