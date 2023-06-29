pipeline {
    agent any
    environment {
        CREDENTIAL_GITLAB_ID = 'gitlab'
        GITLAB_SERVER = 'https://gitlab.ingeek.com/hz/device/generic'
    }
    
    parameters {
        booleanParam(name: 'ota_master_ble', defaultValue:false, description: '构建master ble ota file')
        booleanParam(name: 'ota_slave_ble', defaultValue:false, description: '构建slave ble ota file')
        booleanParam(name: 'ota_mcu_flash_bootloader_mcu', defaultValue:false, description: '构建mcu ota file')
        booleanParam(name: 'send_email', defaultValue:false, description: 'send email')
        text(
            description: 'bootloader version',
            name: 'bootloaderversion', 
            defaultValue: '''V1.0.0''', 
        )
        text(
            description: 'ble sam tag',
            name: 'bleSamTag', 
            defaultValue: '''SAM_BLE_V2.0.2''', 
        ) 
        text(
            description: 'ble sbm tag',
            name: 'bleSbmTag', 
            defaultValue: '''SBM_BLE_V2.0.4''', 
        ) 
        text(
            description: 'mcu tag',
            name: 'mcutag', 
            defaultValue: '''H53_SBM_MCU_V2.2.0''', 
        )

        text(
            description: 'mail',
            name: 'mailbody', 
            defaultValue: '''<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title></title>
</head>
<body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4" offset="0">
<table width="30%"  border = "1" rules = "all" cellpadding="10" cellspacing="10" style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif"> 
    <tr>
        <td>version</td>
        <td>( ${PROJECT_NAME} build ${BUILD_NUMBER})</td>
    </tr>
    <tr>
        <td>change:</td>
        <td>xxx</td>
    </tr>

    <tr>
        <td>test</td>
        <td>pass</td>
    </tr>       
</table> 
<table width="95%" cellpadding="0" cellspacing="0" style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif">  
    <tr>
        <td><br/>
            <b><font color="#0B610B">build information</font></b>
            <hr size="2" width="100%" align="center"/>
        </td>
    </tr>
    <tr>
        <td>
            <ul>
                <li>itemname:${PROJECT_NAME}</li>
                <li>buildno:${BUILD_NUMBER}</li>
                <li>buildreasion: ${CAUSE}</li>
                <li>status:${BUILD_STATUS}</li>
                <li>url:<a href="${BUILD_URL}artifact">download</a></li>
            </ul>
        </td>
    </tr>
</table>
</body>
</html>

''', 
        )
    }
    
    stages {
        stage('CleanWorkspace') {
            steps {
                cleanWs()
                echo 'CleanWorkspace'
            }
        }
        stage('Clone') {
            steps {
                script {
                    if ("$ota_master_ble" == "true" || "$ota_slave_ble" == "true") {
                        echo 'clone ble'
                        dir('a20_sbm_ble_tlsr8273_project') {
                            script {
                                echo 'a20_sbm_ble_tlsr8273_project'
                                git branch: 'develop', credentialsId: "${CREDENTIAL_GITLAB_ID}",\
                                url: "${GITLAB_SERVER}"+'/a20_sbm_ble_tlsr8273_project.git'
                            }
                        }
                    }
                    if ("$ota_mcu_flash_bootloader_mcu" == "true") {
                        dir('KF32A156_Bootloader') {
                            script {
                                echo 'clone bootloader'
                                git branch: 'master', credentialsId: "${CREDENTIAL_GITLAB_ID}",\
                                url: "${GITLAB_SERVER}"+'/kf32a156_bootloader.git'
                            }
                        }
                        dir('a20_sbm_mcu_kf32a156_project') {
                            script {
                            echo 'Clone Mcu'
                                echo 'a20_sbm_mcu_kf32a156_project'
                                sh "git clone --recurse-submodules https://gitlab.ingeek.com/vrd/vid/project/voyah/voyah_h53.git"
                                dir('voyah_h53'){
                                    sh "git checkout ${mcutag}"
                                    sh "git submodule update"
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('CloneRepository') {
            steps {
                script {
                    echo 'Clone jenkins'
                    dir('ingeek_device_jenkins_project') {
                        script {
                            git branch: 'voyah_h53', credentialsId: "${CREDENTIAL_GITLAB_ID}",\
                            url: 'https://gitlab.ingeek.com/vrd/vid/tools/jenkins.git'
                        }
                    }
                }
            }
        }
        stage('build') {
            steps {
                script {
                    if ("$ota_master_ble" == "true") {
                        dir('a20_sbm_ble_tlsr8273_project') {
                            script {
                                sh "git checkout ."
                                sh "git checkout ${bleSbmTag}"
                            }
                        }
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'buildmasterBle'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\build_ble.py -t master -i h53 -v $bleSbmTag"
                            }
                        }
                    }
                    if ("$ota_slave_ble" == "true") {
                        dir('a20_sbm_ble_tlsr8273_project') {
                            script {
                                sh "git checkout ."
                                sh "git checkout ${bleSamTag}"
                            }
                        }
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'buildslaveBle'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\build_ble.py -t slave -i h53 -v $bleSamTag"
                            }
                        }
                    }
                    if ("$ota_mcu_flash_bootloader_mcu" == "true") {
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'buildMcu'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\build_mcu.py"                                
                                echo 'buildBootloader'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\build_bootloader.py"
                            }
                        }
                    }
                }

            }
        }
        stage('post') {
            steps {
                script {
                    if ("$ota_master_ble" == "true") {
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'postmasterBle'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_ble.py -t master -v $bleSbmTag"
                            }
                        }
                    }

                    if ("$ota_slave_ble" == "true") {
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'postslaveBle'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_ble.py -t slave -v $bleSamTag"
                            }
                        }
                    }

                    if ("$ota_mcu_flash_bootloader_mcu" == "true") {
                        dir('ingeek_device_jenkins_project'){
                            script {
                                echo 'post mcu'
                                bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_mcu.py"                                
                            }
                        }
                    }
                }
            }
        }
        stage('merge'){
            steps{
                dir('ingeek_device_jenkins_project'){
                    script {
                        dir('voyah_h53/merges19'){
                            bat "Merge.bat" 
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe s19Parser.py"
                            }
                        if("$ota_mcu_flash_bootloader_mcu" == "true"&&"$ota_slave_ble" == "true"&&"$ota_master_ble" == "true"){
                            echo '1'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $mcutag $bleSamTag $bleSbmTag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "true"&&"$ota_slave_ble" == "true"&&"$ota_master_ble" == "false"){
                            echo '2'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $mcutag $bleSamTag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "true"&&"$ota_slave_ble" == "false"&&"$ota_master_ble" == "true"){
                            echo '3'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $mcutag $bleSbmTag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "false"&&"$ota_slave_ble" == "true"&&"$ota_master_ble" == "true"){
                            echo '4'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $bleSamTag $bleSbmTag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "true"&&"$ota_slave_ble" == "false"&&"$ota_master_ble" == "false"){
                            echo '5'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $mcutag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "false"&&"$ota_slave_ble" == "true"&&"$ota_master_ble" == "false"){
                            echo '6'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $bleSamTag"
                        }
                        else if("$ota_mcu_flash_bootloader_mcu" == "false"&&"$ota_slave_ble" == "false"&&"$ota_master_ble" == "true"){
                            echo '7'
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_merge.py -v $bleSbmTag"
                        }
                    }
                }
            }
        }
        stage('boot'){
            steps{
                script {
                    if ("$ota_mcu_flash_bootloader_mcu" == "true") {
                        dir('ingeek_device_jenkins_project') {
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\post_bootloader.py"
                            bat "C:\\Users\\yinji\\AppData\\Local\\Programs\\Python\\Python36\\python.exe voyah_h53\\merge_bootloader_app.py -vb $bootloaderversion -t $mcutag"
                        }
                    }
                }
            }
        }
        stage('Delivery'){
            steps{
                dir('ingeek_device_jenkins_project\\voyah_h53\\output') {
                    echo 'Achive'
                    archiveArtifacts artifacts: '*.*', followSymlinks: false
                }
            }
        }
    }
    post {
        success {
            script {
                echo 'sucess'
                if ("$send_email" == "true") {
                    emailext(
                        body: '${mailbody}', 
                        subject: '${PROJECT_NAME} ${BUILD_STATUS} ${BUILD_NUMBER}', 
                        mimeType: 'text/html',
                        attachmentsPattern: 'ingeek_device_jenkins_project/voyah_h53/output/*.*',
                        to: 'fei.li@ingeek.com;tianhua.ming@ingeek.com;pengfei.chai@ingeek.com;feifei.huang@ingeek.com;lingguo.xia@ingeek.com;bingjie.li@ingeek.com;lixin.hu@ingeek.com;fuqiang.zhu@ingeek.com;yong.liu@ingeek.com' 
                    )  
                }
            }
        }
        unsuccessful {
            script {
                echo 'unsucessful'
                if ("$send_email" == "true") {
                    emailext(
                        body: '${mailbody}', 
                        subject: '${PROJECT_NAME} ${BUILD_STATUS} ${BUILD_NUMBER}', 
                        mimeType: 'text/html',
                        to: 'fei.li@ingeek.com;tianhua.ming@ingeek.com;pengfei.chai@ingeek.com;feifei.huang@ingeek.com;lingguo.xia@ingeek.com;bingjie.li@ingeek.com;lixin.hu@ingeek.com;fuqiang.zhu@ingeek.com;yong.liu@ingeek.com' 
                    )  
                }
            }
        }
    }
}
