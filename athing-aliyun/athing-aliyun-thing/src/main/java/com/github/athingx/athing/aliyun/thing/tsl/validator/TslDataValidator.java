package com.github.athingx.athing.aliyun.thing.tsl.validator;

import com.github.athingx.athing.aliyun.thing.tsl.schema.TslData;
import com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType;
import com.github.athingx.athing.aliyun.thing.tsl.schema.TslElement;
import com.github.athingx.athing.aliyun.thing.tsl.schema.TslSchema;
import com.github.athingx.athing.aliyun.thing.tsl.specs.ArraySpecs;
import com.github.athingx.athing.aliyun.thing.tsl.specs.StructSpecs;

import static com.github.athingx.athing.aliyun.framework.util.CommonUtils.isIn;
import static com.github.athingx.athing.aliyun.thing.tsl.schema.TslDataType.Type.*;

/**
 * 数据验证器
 */
public interface TslDataValidator {

    /**
     * 校验器集合
     */
    TslDataValidator validator = new TslDataValidator() {

        private final TslDataValidator[] validators = new TslDataValidator[]{
                arrayContainLimit(),
                structContainLimit()
        };

        @Override
        public void validate(TslElement element, TslDataType dataType) {
            for (final TslDataValidator validator : validators) {
                validator.validate(element, dataType);
            }
        }
    };

    /**
     * 验证array的元素是否符合限制要求
     *
     * @return 数据验证器
     */
    static TslDataValidator arrayContainLimit() {
        return new BaseValidator((element, dataType) -> {

            // 过滤掉非array
            if (dataType.getType() != ARRAY) {
                return;
            }

            final ArraySpecs specs = (ArraySpecs) dataType.getSpecs();
            final TslDataType itemDataType = specs.getItem();

            if (!isIn(itemDataType.getType(), INT, FLOAT, DOUBLE, TEXT, STRUCT)) {
                throw new TslDataValidatorException(String.format(
                        "validate error, ARRAY not allow item type %s in %s",
                        itemDataType.getType(),
                        element
                ));
            }

        });
    }

    /**
     * 验证struct的元素是否符合限制要求
     *
     * @return 数据验证器
     */
    static TslDataValidator structContainLimit() {
        return new BaseValidator((element, dataType) -> {

            // 过滤掉非struct
            if (dataType.getType() != STRUCT) {
                return;
            }

            ((StructSpecs) dataType.getSpecs()).stream()
                    .filter(dataInStruct -> !isIn(dataInStruct.getDataType().getType(), INT, FLOAT, DOUBLE, ENUM, BOOL, TEXT, DATE))
                    .forEach(dataInStruct -> {
                        throw new TslDataValidatorException(String.format(
                                "validate error, STRUCT not allow item %s in %s",
                                dataInStruct,
                                element
                        ));
                    });

        });
    }

    static void validates(TslSchema schema) {

        schema.getServices().forEach(service -> {
            service.getInputData().forEach(data -> validator.validate(service, data.getDataType()));
            service.getOutputData().forEach(data -> validator.validate(service, data.getDataType()));
        });

        schema.getEvents().forEach(event ->
                event.getOutputData().forEach(data -> validator.validate(event, data.getDataType())));

        schema.getProperties().forEach(property ->
                validator.validate(property, property.getDataType()));

    }

    /**
     * 验证元素和元素的数据类型是否匹配
     *
     * @param element  元素
     * @param dataType 元素拥有的数据类型
     */
    void validate(TslElement element, TslDataType dataType);

    class BaseValidator implements TslDataValidator {

        private final TslDataValidator validator;

        public BaseValidator(TslDataValidator validator) {
            this.validator = validator;
        }

        @Override
        public void validate(TslElement element, TslDataType dataType) {
            validator.validate(element, dataType);
            if (dataType.getSpecs().getType() == ARRAY) {
                validator.validate(element, ((ArraySpecs) dataType.getSpecs()).getItem());
            }
            if (dataType.getSpecs().getType() == STRUCT) {
                for (TslData dataInStruct : ((StructSpecs) dataType.getSpecs())) {
                    validator.validate(element, dataInStruct.getDataType());
                }
            }
        }

    }

}
