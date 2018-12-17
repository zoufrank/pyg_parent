package com.pyg.group;

import com.pyg.pojo.TbSpecification;
import com.pyg.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {
    private TbSpecification specification;

    public List<TbSpecificationOption> getSpecificationOption() {
        return specificationOption;
    }

    public void setSpecificationOption(List<TbSpecificationOption> specificationOption) {
        this.specificationOption = specificationOption;
    }

    private List<TbSpecificationOption> specificationOption;

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }


}
