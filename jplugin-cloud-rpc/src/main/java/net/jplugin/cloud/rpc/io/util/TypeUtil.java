package net.jplugin.cloud.rpc.io.util;


import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class TypeUtil{

    /**
     * 克隆转换成为能序列化的Type
     * @param temp
     * @return
     */
    public static Type deepClone(Type temp){
        if (temp==null)
            return null;
        if (temp instanceof Serializable)
            return temp;
        else{
            if (temp instanceof GenericArrayType){
                return deepCloneArrayType((GenericArrayType)temp);
            }else if (temp instanceof ParameterizedType){
                return deepCloneParameterlizeType((ParameterizedType)temp);
            }else{
                throw new RuntimeException(temp.getTypeName() +" is not supported!");
            }
        }
    }

    private static Type deepCloneParameterlizeType(ParameterizedType temp) {
        Type[] at = temp.getActualTypeArguments();
        Type[] argTypes = new Type[at.length];
        for (int i=0;i<argTypes.length;i++){
            argTypes[i] = deepClone(at[i]);
        }
        return ParameterizedTypeImpl.make((Class<?>) deepClone(temp.getRawType()),argTypes,deepClone(temp.getOwnerType()));
    }

    private static Type deepCloneArrayType(GenericArrayType temp) {
        return GenericArrayTypeImpl.make(deepClone(temp.getGenericComponentType()));
    }

    public static class GenericArrayTypeImpl implements GenericArrayType, Serializable {
        private final Type genericComponentType;

        private GenericArrayTypeImpl(Type componentType) {
            this.genericComponentType = componentType;
        }

        public static GenericArrayTypeImpl make(Type ct) {
            return new GenericArrayTypeImpl(ct);
        }


        public Type getGenericComponentType() {
            return this.genericComponentType;
        }

        public String toString() {
            Type var1 = this.getGenericComponentType();
            StringBuilder var2 = new StringBuilder();
            if (var1 instanceof Class) {
                var2.append(((Class)var1).getName());
            } else {
                var2.append(var1.toString());
            }

            var2.append("[]");
            return var2.toString();
        }

        public boolean equals(Object obj) {
            if (obj instanceof GenericArrayType) {
                GenericArrayType var2 = (GenericArrayType)obj;
                return Objects.equals(this.genericComponentType, var2.getGenericComponentType());
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hashCode(this.genericComponentType);
        }
    }

    public static  class ParameterizedTypeImpl implements ParameterizedType, Serializable {
        private final Type[] actualTypeArguments;
        private final Class<?> rawType;
        private final Type ownerType;

        private ParameterizedTypeImpl(Class<?> aRawType, Type[] aActureTypeAgs, Type aOwnerType) {
            this.actualTypeArguments = aActureTypeAgs;
            this.rawType = aRawType;
            this.ownerType = (Type)(aOwnerType != null ? aOwnerType : aRawType.getDeclaringClass());
        }



        public static ParameterizedTypeImpl make(Class<?> aRawType, Type[] aActureTypeAgs, Type aOwnerType) {
            return new ParameterizedTypeImpl(aRawType, aActureTypeAgs, aOwnerType);
        }

        public Type[] getActualTypeArguments() {
            return (Type[])this.actualTypeArguments.clone();
        }

        public Class<?> getRawType() {
            return this.rawType;
        }

        public Type getOwnerType() {
            return this.ownerType;
        }

        public boolean equals(Object type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if (this == parameterizedType) {
                    return true;
                } else {
                    Type var3 = parameterizedType.getOwnerType();
                    Type var4 = parameterizedType.getRawType();
                    return Objects.equals(this.ownerType, var3) && Objects.equals(this.rawType, var4) && Arrays.equals(this.actualTypeArguments, parameterizedType.getActualTypeArguments());
                }
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (this.ownerType != null) {
                if (this.ownerType instanceof Class) {
                    buffer.append(((Class)this.ownerType).getName());
                } else {
                    buffer.append(this.ownerType.toString());
                }

                buffer.append(".");
                if (this.ownerType instanceof ParameterizedTypeImpl) {
                    buffer.append(this.rawType.getName().replace(((ParameterizedTypeImpl)this.ownerType).rawType.getName() + "$", ""));
                } else {
                    buffer.append(this.rawType.getName());
                }
            } else {
                buffer.append(this.rawType.getName());
            }

            if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
                buffer.append("<");
                boolean first = true;
                Type[] arguments = this.actualTypeArguments;
                int var4 = arguments.length;

                for(int idx = 0; idx < var4; ++idx) {
                    Type var6 = arguments[idx];
                    if (!first) {
                        buffer.append(", ");
                    }

                    buffer.append(var6.getTypeName());
                    first = false;
                }

                buffer.append(">");
            }

            return buffer.toString();
        }
    }

}
