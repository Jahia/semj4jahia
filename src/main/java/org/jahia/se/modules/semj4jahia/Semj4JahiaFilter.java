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
//    private final static String SEMJ4JAHIA_MKTSCRIPTNAME="trackingScriptUTM.js";
//    private final static String SEMJ4JAHIA_USER_COOKIE_NAME = "internal_jahian_user";
//    private final static String SEMJ4JAHIA_USER_COOKIE_VISITOR_VALUE = "0";
//    private final static String SEMJ4JAHIA_USER_COOKIE_JAHIANS_VALUE = "1";
//
//    private final static String PAGE_CATEGORY_1_PROPS="pageType";
//    private final static String PAGE_CATEGORY_2_PROPS="j:nodename";

    @Activate
    public void activate() {
        setPriority(0);// -1 launch after addStuff
        setApplyOnModes("edit");//,preview
        setApplyOnConfigurations("page");
        setApplyOnTemplateTypes("html");
        setSkipOnConfigurations("include,wrapper");//?
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String output = super.execute(previousOut, renderContext, resource, chain);
        boolean isInstalled = false;

        JCRPropertyWrapper installedModules = renderContext.getSite().getProperty("j:installedModules");

        for (JCRValueWrapper module : installedModules.getValues()) {
            if (SEMJ4JAHIA_MODULE.equals(module.getString())) {
                isInstalled = true;
                break;
            }
        }

        //Disable the filter in case we are in Content Editor preview.
        boolean isCEPreview = renderContext.getRequest().getAttribute("ce_preview") != null;

        if(isInstalled && !isCEPreview){
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

//        //Add context script and html hook to the BODY tag
//        List<Element> elementList = source.getAllElements(HTMLElementName.BODY);
//        if (elementList != null && !elementList.isEmpty()) {
////            final StartTag bodyStartTag = elementList.get(0).getStartTag();
////            outputDocument.replace(bodyStartTag.getEnd(), bodyStartTag.getEnd() + 1, gtBodyScript);
//
//            final EndTag bodyEndTag = elementList.get(0).getEndTag();
//            outputDocument.insert(bodyEndTag.getBegin(), getBodyHtmlHook(previewURI));
//        }


//        //Add webapp script to the HEAD tag
        List<Element> elementList = source.getAllElements(HTMLElementName.HEAD);
        if (elementList != null && !elementList.isEmpty()) {
            final StartTag headStartTag = elementList.get(0).getStartTag();
            outputDocument.insert(headStartTag.getEnd(),getHeadScript(previewURI));
        }

        output = outputDocument.toString().trim();
        return output;
    }
//    private String getBodyHtmlHook(String url){
//        String style = "";//"border:0;visibility:hidden;";
////        String onLoad = "()=>{const iframeContent = this.contentDocument || this.contentWindow.document;console.log(iframeContent.body.innerHTML);}";
//        String onLoad = "()=>{console.log('iframe loaded',this)}";
//        return "\n<iframe onLoad=\""+onLoad+"\" id=\"jahiaPagePreview\" width=\"100\" height=\"100\" src=\""+url+"\" style=\""+style+"\"/>";
////        return "\n<iframe id=\"jahiaPagePreview\" src=\""+url+"\" ></iframe>";
//    }

    private String getHeadScript(String previewURI) throws RepositoryException, IOException {
        StringBuilder headScriptBuilder =
                new StringBuilder("\n<script type=\"application/javascript\">window.semj4 = window.semj4 || {src:\""+previewURI+"\"};");
        headScriptBuilder.append( "\n</script>");

        InputStream resourceAsStream = WebUtils.getResourceAsStream("/modules/semj4jahia/javascript/"+SEMJ4JAHIA_SCRIPTNAME);
        String checksum = resourceAsStream != null ? FileUtils.calculateDigest(resourceAsStream) : "0";
        headScriptBuilder.append( "\n<script async type=\"text/javascript\" src=\"/modules/semj4jahia/javascript/"+SEMJ4JAHIA_SCRIPTNAME+"?version="+checksum+"\"></script>" );

        return headScriptBuilder.toString();
    }
//
//    private List<String> getPageCategories(RenderContext renderContext) throws RepositoryException {
//        List<String> pageCategories = new ArrayList<String>();
//        String siteName = renderContext.getSite().getName();
//        JCRNodeWrapper mainResourceNode = renderContext.getMainResource().getNode();
//        JCRNodeWrapper pageNode;
//
//        if(mainResourceNode.isNodeType("jnt:page")) {
//            pageNode = mainResourceNode;
//        }else{
//            pageNode = JCRContentUtils.getParentOfType(mainResourceNode,"jnt:page");
//        }
//
//        if(pageNode != null) {
//            pageCategories.add(
//                pageNode.hasProperty(PAGE_CATEGORY_1_PROPS) ?
//                    pageNode.getProperty(PAGE_CATEGORY_1_PROPS).getValue().getNode().getProperty("jcr:title").getValue().toString()
//                    : siteName
//            );
//
//            pageCategories.add(pageNode.getProperty(PAGE_CATEGORY_2_PROPS).getValue().toString());
//            pageCategories.add(pageNode.getIdentifier());
//            pageCategories.add(pageNode.getPath());
//        }
//        return pageCategories;
//    }

//    private void manageCookie(RenderContext renderContext){
//        HttpServletRequest httpServletRequest = renderContext.getRequest();
//        List<Cookie> cookieNextPreviewList = new ArrayList<>();
//        Cookie[] cookies = httpServletRequest.getCookies();
//
//        if(cookies != null && cookies.length > 0){
//            cookieNextPreviewList = Arrays.stream(cookies) // convert list to stream
//                    .filter(cookie -> cookie.getName().equals(SEMJ4JAHIA_USER_COOKIE_NAME))
//                    .collect(Collectors.toList());
//        }
//
//        //add cookie
//        if (cookieNextPreviewList.isEmpty()){
//            if(isJahians(renderContext)){
//                renderContext.getResponse().addCookie(
//                    buildCookie(SEMJ4JAHIA_USER_COOKIE_JAHIANS_VALUE,getCookiePath(httpServletRequest))
//                );
//            }else{
//                renderContext.getResponse().addCookie(
//                    buildCookie(SEMJ4JAHIA_USER_COOKIE_VISITOR_VALUE,getCookiePath(httpServletRequest))
//                );
//            }
//        }else{
//            //update cookie if needed
//            Cookie cookie = cookieNextPreviewList.get(0);
//            if(cookie.getValue().equals(SEMJ4JAHIA_USER_COOKIE_VISITOR_VALUE) && isJahians(renderContext))
//                renderContext.getResponse().addCookie(
//                    buildCookie(SEMJ4JAHIA_USER_COOKIE_JAHIANS_VALUE,getCookiePath(httpServletRequest))
//                );
//        }
//    }

//    private Cookie buildCookie(String value, String path){
//        Cookie cookie = new Cookie(SEMJ4JAHIA_USER_COOKIE_NAME,value);
//        cookie.setPath(path);
//        cookie.setMaxAge(365*24*60*60);//1y in sec
//        return cookie;
//    }
//    private boolean isGuest(RenderContext renderContext){
//        JahiaUser user = renderContext.getUser();
//        return "guest".equals(user.getName());
//    }
//    private boolean isJahians(RenderContext renderContext){
//        JahiaUser user = renderContext.getUser();
//        String email = user.getProperty("j:email");
//        return (StringUtils.isNotEmpty(email) && email.contains("@jahia.com"));
//    }
//    private String getCookiePath(HttpServletRequest httpServletRequest){
//        String cookiePath = StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ?
//                httpServletRequest.getContextPath() : "/";
//        return cookiePath;
//    }
}
