package com.powerapi;

import com.powerapi.dao.GitDao;
import com.powerapi.service.PowerapiService;
import com.powerapi.utils.CommonUtils;
import com.powerapi.utils.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Says "Hi" to the user.
 */
@Mojo(name = "test")
public class PowerapiMojo extends AbstractMojo {

    @Parameter(property = "test.build")
    private String build;

    @Parameter(property = "test.esUrl")
    private String esUrl;

    @Parameter(property = "test.commit")
    private String commit;

    @Parameter(property = "test.scm")
    private String scm;

    @Parameter(property = "test.frequency")
    private Integer frequency;

    @Parameter(property = "test.jenkins")
    private String jenkins;

    private PowerapiService powerapiService = new PowerapiService();
    private GitDao gitDao = GitDao.getInstance();

    private List<String> powerapiCSVList = new ArrayList<>();
    private List<String> testCSVList = new ArrayList<>();

    public void execute() throws MojoExecutionException {
        Logger.setLog(getLog());

        if(build == null) throw new MojoExecutionException("No build name found, type: -Dtest.build=\"build_name\"");
        else if(esUrl == null) throw new MojoExecutionException("No ElasticSearch url found, type: -Dtest.esUrl=\"ElasticSearch url serveur\"");
        else if(frequency == null) throw new MojoExecutionException("No frequency found, type: -Dtest.frequency=[50-oo]");

        if(commit == null) {
            Logger.warning("No commit name: work with git for have commit name");
            commit = gitDao.getCommitName();
            Logger.info("Commit name: "+commit);
        }

        Long beginApp = new Date().getTime();

        executes();
        powerapiService.sendPowerapiciData(beginApp, "MASTER", build, commit, scm, powerapiCSVList, testCSVList);

        getLog().info("Data send");
    }

    private void executes() {
       // String[] cmd = {"sh", "-c", "echo toto > untest.csv; (mvn test -DforkCount=0 | grep timestamp= | cut -d '-' -f 2 | tr -d ' ') > test1.csv & powerapi duration 30 modules procfs-cpu-simple monitor --frequency 50 --console --pids \\$! | grep muid) > data1.csv;"};
        String[] cmd1 = {"sh", "-c", "(mvn test -DforkCount=0 | grep timestamp= | cut -d '-' -f 2 | tr -d ' ') > test1.csv & (powerapi duration 30 modules procfs-cpu-simple monitor --frequency 50 --console --all | grep muid) > data1.csv;"};

        try {
            getLog().info("En cours d'execution...");
            Process p = Runtime.getRuntime().exec(cmd1);
            getLog().info(CommonUtils.readProcessus(p));

            p.waitFor();
            Process powerapiProc = Runtime.getRuntime().exec(new String[]{"sh", "-c", "cat data1.csv | tr '\n' ' '"});
            powerapiCSVList.add(CommonUtils.readProcessus(powerapiProc));

            Process testProc = Runtime.getRuntime().exec(new String[]{"sh", "-c", "cat test1.csv | grep timestamp= | cut -d '-' -f 2 | tr -d ' '"});
            testCSVList.add(CommonUtils.readProcessus(testProc));
        } catch (IOException | InterruptedException e) {
            getLog().error("", e);
        }
    }
}