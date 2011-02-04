package org.atlasapi.remotesite.pa.bindings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pictureUsage"
})
@XmlRootElement(name = "pictures")
public class Pictures {

    @XmlElement(required = true)
    protected List<PictureUsage> pictureUsage;

    public List<PictureUsage> getPictureUsage() {
        if (pictureUsage == null) {
            pictureUsage = new ArrayList<PictureUsage>();
        }
        return this.pictureUsage;
    }
}
