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
/**
 *
 * @exclude
 * @hide
 *
 * */
public class ArrayHandler extends HandlerBase implements IContentHandler<List<Object>> {
    List<Object> values;
    List<Object> list;

    Integer level = -1;

    Boolean isStartArray = false;

    List<Object> target = null;

    public ArrayHandler() {
        this.level = -1;
    }

    public boolean startArray() throws ParseException, IOException {
        this.level++;
        this.isStartArray = true;
        //只赋值一次
        if (this.values == null) {
            this.values = new ArrayList();
        }

        target = this.values;

        for (Integer i = 0; i < this.level; i++) {

            if (target.size() > 0) {


                if (NumberUtil.equals(this.level - 1, i)) {

                    List<Object> newStartArray = new ArrayList();
                    target.add(newStartArray);
                    target = newStartArray;
                } else {
                    Object lastChild = target.get(target.size() - 1);

                    target = (List<Object>) lastChild;

                }

            } else {
                target.add(new ArrayList());
                target = (List<Object>) target.get(0);
            }
        }

        return true;
    }

    public boolean primitive(Object value) throws ParseException, IOException {
        if (this.values != null && target != null) {
            target.add(value);
            return true;
        } else {
            return super.primitive(value);
        }
    }

    public boolean endArray() throws ParseException, IOException {
        this.isStartArray = false;
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
