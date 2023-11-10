package org.jahia.se.modules.semj4jahia;

import net.htmlparser.jericho.*;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.FileUtils;
import org.jahia.utils.WebUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component(service = RenderFilter.class)
public class Semj4JahiaFilter extends AbstractFilter {
    private static Logger logger = LoggerFactory.getLogger(Semj4JahiaFilter.class);
    private final static String SEMJ4JAHIA_MODULE="semj4jahia";
    private final static String SEMJ4JAHIA_SCRIPTNAME="semj4jahia.js";
    private final static String SEMJ4JAHIA_IFRAMEID="jahiaPagePreview4Semj";

    @Activate
    public void activate() {
        setPriority(0);
        setApplyOnModes("edit");
        setApplyOnConfigurations("page");
        setApplyOnTemplateTypes("html");
        setSkipOnConfigurations("include,wrapper");//?
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String output = super.execute(previousOut, renderContext, resource, chain);

        //Disable the filter in case we are in Content Editor preview.
        boolean isCEPreview = renderContext.getRequest().getAttribute("ce_preview") != null;

        if(!isCEPreview){
            //update output to add scripts
            output = enhanceOutput(output, renderContext);
        }

        return output;
    }

    /**
     * This Function is just to add some logic to our filter and therefore not needed to declare a filter
     *
     * @param output    Original output to modify
     * @return          Modified output
     */
    @NotNull
    private String enhanceOutput(String output, RenderContext renderContext) throws Exception{

        Source source = new Source(output);
        OutputDocument outputDocument = new OutputDocument(source);

        String previewURI = renderContext.getRequest().getRequestURI().replace("editframe","render");

        //Add webapp script to the HEAD tag
        List<Element> elementList = source.getAllElements(HTMLElementName.HEAD);
        if (elementList != null && !elementList.isEmpty()) {
            final StartTag headStartTag = elementList.get(0).getStartTag();
            outputDocument.insert(headStartTag.getEnd(),getHeadScript(previewURI,isModuleEnabled(renderContext)));
        }

        output = outputDocument.toString().trim();
        return output;
    }

    private String getHeadScript(String previewURI, boolean isModuleEnabled) throws RepositoryException, IOException {
        StringBuilder headScriptBuilder =
                new StringBuilder("\n<script type=\"application/javascript\">");
        headScriptBuilder.append( "\nwindow.semj4 = {");
        headScriptBuilder.append( "\n src:\""+previewURI+"\"");
        headScriptBuilder.append( ",\n isModuleEnabled:"+isModuleEnabled);
        headScriptBuilder.append( ",\n frameId:\""+SEMJ4JAHIA_IFRAMEID+"\"");
        headScriptBuilder.append( "\n};");
        headScriptBuilder.append( "\n</script>");

        InputStream resourceAsStream = WebUtils.getResourceAsStream("/modules/semj4jahia/javascript/"+SEMJ4JAHIA_SCRIPTNAME);
        String checksum = resourceAsStream != null ? FileUtils.calculateDigest(resourceAsStream) : "0";
        headScriptBuilder.append( "\n<script async type=\"text/javascript\" src=\"/modules/semj4jahia/javascript/"+SEMJ4JAHIA_SCRIPTNAME+"?version="+checksum+"\"></script>" );

        return headScriptBuilder.toString();
    }

    private boolean isModuleEnabled(RenderContext renderContext) throws RepositoryException {
        boolean isModuleEnabled = false;

        JCRPropertyWrapper installedModules = renderContext.getSite().getProperty("j:installedModules");

        for (JCRValueWrapper module : installedModules.getValues()) {
            if (SEMJ4JAHIA_MODULE.equals(module.getString())) {
                isModuleEnabled = true;
                break;
            }
        }
        return isModuleEnabled;
    }

}
