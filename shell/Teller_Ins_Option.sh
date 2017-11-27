#!/bin/bash
#source ~/.bashrc

echo **********************************************************
echo **                                                      **
echo **            Teller9 Ins Deploy Shell                  **
echo **              http://www.dcits.com                    **
echo **            author:chenkunh@dcits.com                 **
echo **                                                      **
echo **********************************************************

# 原bat增量出去脚本思路描述：
# 1、根据开发人员提交的txt增量描述文件生成要编译的交易配置文件bulid.properties
# 2、开始进行增量交易的编译
# 3、进行增量抽取工作
# 4、将增量目标码打成压缩包待部署

#################### Var Setting START ####################
SIGN_FLAG=Y
SIGN_PWD=Y
FILE_PATH=`pwd`
RUNDATE=`date +%Y%m%d`
TAG_NO=02
BUILD_PATH=${FILE_PATH}
BUILD_PROPERTIES=${BUILD_PATH}/build.properties
INCFILE=${FILE_PATH}/RUNALL/app_${RUNDATE}${TAG_NO}.txt
ANT_HOME=${FILE_PATH}/tools\ant\

MSG_NOT_EXIST_PROPERTIES='不存在增量执行文件build.properties，不可以进行打增量版本'
MSG_NOT_EXIST_INCFILE='不存在增量清单文件，不可以进行打增量版本'
strA="SmartTeller9\trans"
strB=".jar"
FIRST=0
BUILD=""
TEMP=""
#################### Var Setting END ####################

export ANT_HOME=${ANT_HOME}
cd ${BUILD_PATH}
##检查增量执行文件是否存在
if [ ! -e "build.properties" ]; then 
	echo $MSG_NOT_EXIST_PROPERTIES
	exit 0
fi

#echo $RUNDATE
##检查增量清单是否存在
if [ ! -e "$INCFILE" ]; then 
	echo $MSG_NOT_EXIST_INCFILE
	exit 1
fi

# 读取增量描述文件，进行增量配置文件的处理
for line in $(cat ${INCFILE})
do 
    if [[ "$line" =~ "${strA}" ]]
    then
        #echo "包含SmartTeller9\trans"
        if [[ "$line" =~ "${strB}" ]]
        then
    #		echo "包含jar"
            if test ${FIRST} -ne 0;then
                BUILD=`echo ${TEMP},${line}`
            else
                BUILD=`echo ${line}`
                FIRST=1
            fi
            TEMP=${BUILD}
#        else
#            echo "不包含jar"
        fi
#    else
#	echo "不包含SmartTeller9\trans"
    fi
done

#筛选需要编译的交易
BUILD=${BUILD//SmartTeller9\\trans\\/}
BUILD=${BUILD//.jar/}
echo "需要打版本的交易为："$BUILD
sed -i "/sourceBase=/s/=.*/=${BUILD//\\/\/}/" build.properties

# 进行增量交易的编译
echo "开始编译"
ant -buildfile build.xml
# 进行增量抽取工作


# 进行增量包的发布
