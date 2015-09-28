package org.hspconsortium.platform.messaging.controller;

import org.kie.api.definition.rule.Rule;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.definition.KnowledgePackage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping("/")
public class ManagementController {

    @Inject
    KnowledgeBase knowledgeBase;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        return "Ok";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showKnowledgeBase() {
        StringBuffer packageBuffer = new StringBuffer("Packages: \n");
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            packageBuffer.append(" - " + knowledgePackage.getName() + "\n");

            if (!knowledgePackage.getRules().isEmpty()) {
                packageBuffer.append("    Rules: \n");
                for (Rule rule : knowledgePackage.getRules()) {
                    packageBuffer.append("      - " + rule.getName() + " \n");
                }
            }
        }
        return packageBuffer.toString();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public String reset() {
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            knowledgeBase.removeKnowledgePackage(knowledgePackage.getName());
        }
        return "Reset Successful";
    }


}
