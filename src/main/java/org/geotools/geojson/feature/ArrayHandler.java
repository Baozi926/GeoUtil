//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

/**
 * 用来解决无法读取嵌套数组的问题
 **/
package org.geotools.geojson.feature;

import cn.hutool.core.util.NumberUtil;
import org.geotools.geojson.HandlerBase;
import org.geotools.geojson.IContentHandler;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayHandler extends HandlerBase implements IContentHandler<List<Object>> {
    List<Object> values;
    List<Object> list;

    Integer level = -1;

    public ArrayHandler() {
        this.level = -1;
    }

    public boolean startArray() throws ParseException, IOException {
        this.level++;
        //只赋值一次
        if (this.values == null) {
            this.values = new ArrayList();
        }
        return true;
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        if (this.values != null) {

            List<Object> target = this.values;


            for (Integer i = this.level; i > 0; i--) {

                if (target.size() > 0) {
//                    if(i)
                    if (NumberUtil.equals(i, 1)) {
                        Object lastChild = target.get(target.size() - 1);

                        if (lastChild instanceof ArrayList) {
                            List<Object> lastChildArray = (List<Object>) lastChild;

                            lastChildArray.add(new ArrayList<>());

                            target = (List<Object>) lastChildArray.get(lastChildArray.size() - 1);
                        } else {
                            target.add(new ArrayList());
                            target = (List<Object>) target.get(target.size() - 1);
                        }


                    } else {
                        target = (List<Object>) target.get(target.size() - 1);
                    }


                } else {
                    target.add(new ArrayList());
                    target = (List<Object>) target.get(0);
                }
            }


            target.add(value);
            return true;
        } else {
            return super.primitive(value);
        }
    }

    public boolean endArray() throws ParseException, IOException {
        this.level--;
        if (this.level < 0) {
            this.list = this.values;
            this.values = null;
            return true;
        }

        return false;


    }

    public List<Object> getValue() {
        return this.list;
    }
}
