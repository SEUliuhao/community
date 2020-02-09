package life.liuhao.community.community.Controller;

import Model.User;
import life.liuhao.community.community.DTO.AccessTokenDto;
import life.liuhao.community.community.DTO.GithubUser;
import life.liuhao.community.community.Mapper.UserMapper;
import life.liuhao.community.community.Provider.GithubProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Value("${github.client.id}")
    private String clientId;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value(("${github.redirect.url}"))
    private  String redirectUrl;

    @Autowired
    private UserMapper userMapper;



   @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code,
                           @RequestParam(name ="state") String state,
                           HttpServletRequest request){
       AccessTokenDto accessTokenDto = new AccessTokenDto();
       accessTokenDto.setClient_id(clientId);
       accessTokenDto.setClient_secret(clientSecret);
       accessTokenDto.setCode(code);
       accessTokenDto.setRedirect_url(redirectUrl);
       accessTokenDto.setState(state);
       String accessToken = githubProvider.getAccessToken(accessTokenDto);
       GithubUser githubUser = githubProvider.getUser(accessToken);
       System.out.println(githubUser.getName());
       if(githubUser!=null){
           //登录成功，写cookie和session
           User user = new User();
           user.setToken(UUID.randomUUID().toString());
           user.setName(githubUser.getName());;
           user.setAccount_id(String.valueOf(githubUser.getId()));
           user.setGmt_create(System.currentTimeMillis());
           user.setGmt_modified(user.getGmt_create());
           userMapper.insert(user);
           request.getSession().setAttribute("user",githubUser);
           return  "redirect:/";
       }else {
           //登陆失败
           return  "redirect:/";

       }
    }
}
