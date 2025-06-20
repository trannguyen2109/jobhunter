import { useAppSelector } from "@/redux/hooks";
import { Result } from "antd";
import Item from "antd/es/list/Item";
import { useEffect, useState } from "react";

interface IProps{
    hideChildren?: boolean;
    children: React.ReactNode;
    permission: { method: String, apiPath: String, module: String };
}

const Access = (props: IProps) => {
    const { permission, hideChildren = false } = props;
    const [allow, setAllow] = useState<boolean>(true);

    const permissions = useAppSelector(state => state.account.user.role.permissions);

    useEffect(() => {
        if (permissions?.length) {
            const check = permissions.find(item => item.apiPath === permission.apiPath &&
                item.method === permission.method &&
                item.module === permission.module                                                
            )
            if (check) {
                setAllow(true);
            } else {
                setAllow(false);
            }
        }
    },[permissions]
    );
    return (
        <>
            {allow === true || import.meta.env.VITE_ACL_ENABLE === 'false' ?
                <> {props.children}</>
                :
                <>
                    {hideChildren === false ?
                        <Result
                            status="403"
                            title="truy cập bị từ chối"
                            subTitle="xin lỗi , bạn không có quyền hạn (permission) truy cập thông tin này"
                        /> 
                        :
                        <>
                        {/* {render nothing} */}
                        </>
                }
                </>
            }
        </>

    )

}
export default Access;