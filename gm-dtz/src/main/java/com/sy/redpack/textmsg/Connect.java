package com.sy.redpack.textmsg;

import com.sy.redpack.textmsg.util.SHA1;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Controller
public class Connect
{
    private static final String token="lika";
    @RequestMapping(value = "/security" ,method = RequestMethod.GET)
    public void security(@RequestParam String signature,
                         @RequestParam String timestamp,
                         @RequestParam String nonce,
                         @RequestParam String echostr,
                         HttpServletResponse res)
    {
        String[] params={token,timestamp,nonce};
        Arrays.sort(params);
        String tmp=params[0]+params[1]+params[2];
        if(signature.equals(SHA1.encode(tmp)))
        {
            try
            {
                OutputStream out=res.getOutputStream();
                out.write(echostr.getBytes());
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
