package com.maurofokker.security.acl.controller;

import com.maurofokker.security.acl.model.Possession;
import com.maurofokker.security.acl.persistence.PossessionRepository;
import com.maurofokker.security.acl.persistence.UserRepository;
import com.maurofokker.security.acl.security.LocalPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/possessions")
public class PossessionController {

    @Autowired
    private PossessionRepository possessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocalPermissionService permissionService;

    // API

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    public Possession findOne(@PathVariable("id") final Long id) {
        return possessionRepository.findOne(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView create(@Valid Possession possession, Authentication authentication) {
        possession.setOwner(userRepository.findByEmail(authentication.getName()));
        possession = possessionRepository.save(possession);
        System.out.println(possession);
        // permissionService.addPermissionForAuthority(possession, BasePermission.ADMINISTRATION, "ADMIN");
        permissionService.addPermissionForUser(possession, BasePermission.ADMINISTRATION, authentication.getName());
        return new ModelAndView("redirect:/user?message=Possession created with id " + possession.getId());
    }

    //

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(@ModelAttribute final Possession possession) {
        return "tl/possession";
    }

}