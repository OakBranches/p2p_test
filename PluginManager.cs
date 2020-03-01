using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Android;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class PluginManager : MonoBehaviour
{

    [SerializeField] UnityEngine.UI.Button prefabButton;

    [SerializeField] UnityEngine.UI.Button send;

    [SerializeField] UnityEngine.UI.InputField input;

    [SerializeField] Text infoText;

    [SerializeField] GameObject scrollViewContent;
    bool wasConnected;
    UnityEngine.UI.Button[] child;
    int n;
    AndroidJavaObject multi;
    string[] list, oldlist;
    AndroidJavaClass classe;
    string mensagem;

    void Start()
    {
       
        send.onClick.AddListener(goMsg);
        n = 0;
        mensagem = "";
        multi = new AndroidJavaObject("com.vuforia.android.pluginlib.Multi");
        AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
        multi.Call("SetContext", activity);
        multi.Call("InitialWork");

        Debug.Log("começo");

        infoText.text = "label: " + multi.GetStatic<string>("label");
        list = (string[])multi.Call<string[]>("getDeviceNameArray");
        RenovarLista(list);


    }

    private void goMsg()
    {
        if (!input.text.Equals(""))
        {
            sendMsg(input.text);
            Button msg;
            msg = Instantiate(prefabButton);
            msg.transform.SetParent(scrollViewContent.transform, false);
            msg.GetComponentInChildren<Text>().text = input.text;
            msg.name = input.text;
            msg.GetComponentInChildren<Text>().color = Color.green;

            input.text = "";
        }
    }

    public void setPort(int i)
    {
        multi.Call("setPort",i);
    }
    public void setTimeout(int i)
    {
        multi.Call("setTimeout", i);
    }
    public int getPort()
    {
        return multi.Get<int>("port");
    }
    public int getTimeout()
    {
        return multi.Get<int>("timeout");
    }
    public string getMyIp()
    {
        return multi.Get<string>("myIp");
    }
    public string getHostIp()
    {

        return multi.Get<string>("hostIp");
    }
    public bool isHost()
    {

        return multi.Get<bool>("isHost");
    }
    public bool isConnected()
    {

        return multi.Get<bool>("isConnected");
    }
    public void setWifiOn()
    {
        multi.Call("setWifiOn");
    }
    public void setWifiOff()
    {
        multi.Call("setWifiOff");
    }
    public void WifiOnOff()
    {
        multi.Call("WifiOnOff");
    }
    public void Disconnect()
    {
        multi.Call("Disconnect");
    }
    public void ConnectIndexList(int i)
    {
        multi.Call("ConnectIndexList", i);
    }
    public void DiscoverPeers()
    {
        multi.Call("DiscoverPeers");
    }

    public void Apagar()
    {
        if (child == null)
            return;
        for (int i = 0; i<child.Length ; i++)
        {

           
            Destroy(child[i].gameObject,0);
        }
    }
    public void RenovarLista(string [] lista)
    {
        Apagar();

        child = new UnityEngine.UI.Button[lista==null?1: lista.Length+1];


        child[0] = Instantiate(prefabButton);
        child[0].transform.SetParent(scrollViewContent.transform, false);
        child[0].GetComponentInChildren<Text>().text = "DISCOVERY";
        child[0].name = "DISCOVERY";
        child[0].onClick.AddListener(TaskOnClick);
        if (lista == null)
            infoText.color = Color.red;
        else
            infoText.color = Color.cyan;
        for (int i = 1; i <= (lista==null?0: lista.Length); i++)
        {
            child[i] = Instantiate(prefabButton);
            child[i].transform.SetParent(scrollViewContent.transform, false);
            child[i].GetComponentInChildren<Text>().text = lista[i-1];
            child[i].name = lista[i-1];
            child[i].onClick.AddListener(TaskOnClick);
        }
    }
    void TaskOnClick()
    {

        string name = EventSystem.current.currentSelectedGameObject.name;
        //Apagar();   
        Debug.Log(name);
        if (name.Equals("DISCOVERY"))
        {
            Debug.Log("descobrindo............");
            multi.Call("DiscoverPeers");

            infoText.color = Color.green;
            Debug.Log("descobrind2............");
            return;
        }
        for (int i = 0; i < (list==null?0:list.Length); i++)
        {
            if (list[i].Equals(name))
            {
                multi.Call("ConnectIndexList", i);
          
            }
        }
        
    }
    // Update is called once per frame
    IEnumerator Example()
    {

        yield return new WaitForSeconds(5);

    }
    void Update()
    {
        n++;
        if (multi != null)

        {


            //  infoTexttext = "antes";

            //infoTexttext = a + " AMENO";

            //Debug.Log("esse é o teste : "+a);
            //Debug.Log(a);
            string a = multi.GetStatic<string>("label");
            infoText.text = a;
            if (!isConnected())
            {
               

                oldlist = list;
                list = multi.Get<string[]>("deviceNameArray");


                if (n % 200 == 0)
                    RenovarLista(list);
                wasConnected = false;
            }
            else
            {
                if(wasConnected==false)
                    Apagar();
                if (mensagem!=getMsg()&&""!=getMsg())
                {
                    Button msg;
                    msg = Instantiate(prefabButton);
                    msg.transform.SetParent(scrollViewContent.transform, false);
                    msg.GetComponentInChildren<Text>().text = getMsg();
                    msg.GetComponentInChildren<Text>().color = Color.blue;
                    msg.name = getMsg();
                    mensagem = getMsg();
                }
                wasConnected = true;

            }

        }
        else
        {
            infoText.text = "NULLAPP";
            Debug.Log("multi is null");
        }
    }

    public string getMsg()
    {
        if(isConnected())
            return multi.GetStatic<string>("tempMsg");
        return "null";
    }
    public void sendMsg(string s)
    {
        if (isConnected())
        {
            multi.Call("SendMsg", s);
        }
    }
}
