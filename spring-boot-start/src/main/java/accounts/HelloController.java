package accounts;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created on 08 October 2017 @ 2:23 PM
 * Component for project "parentCoreSpringProject"
 *
 * @author Thomas Bezuidenhout
 */
@Controller
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
