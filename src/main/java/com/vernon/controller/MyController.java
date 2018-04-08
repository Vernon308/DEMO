package com.vernon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class MyController {

  @RequestMapping("/welcome")
    public String welcome(){
      return "welcome";

  }
  @RequestMapping("/test")
  public String test(){
    return "test";

  }

}
