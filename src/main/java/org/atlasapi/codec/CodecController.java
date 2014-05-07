package org.atlasapi.codec;

import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

@Controller
public class CodecController {

    private static final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private static final Gson gson = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();
    
    @RequestMapping(value = "/ids/encoded/{id}.json", method = RequestMethod.GET)
    public void decode(HttpServletRequest request, HttpServletResponse response, 
            @PathVariable("id") String id) throws IOException {
        
        try {
            IdCodec decodedId = new IdCodec(codec.decode(id));
            gson.toJson(decodedId, response.getWriter());
        } catch (Exception e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
        
    }
    
    @RequestMapping(value = "/ids/decoded/{id}.json", method = RequestMethod.GET)
    public void encode(HttpServletRequest request, HttpServletResponse response, 
            @PathVariable("id") String id) throws IOException {
        
        try {
            IdCodec encodedId = new IdCodec(codec.encode(BigInteger.valueOf(Long.valueOf(id))));
            gson.toJson(encodedId, response.getWriter());
        } catch (Exception e) {
            response.sendError(HttpStatusCode.BAD_REQUEST.code(), e.getMessage());
        }
    }
    
}
