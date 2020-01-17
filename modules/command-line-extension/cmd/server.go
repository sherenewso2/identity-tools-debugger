/*
Copyright © 2020 NAME HERE <EMAIL ADDRESS>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package cmd

import (
	"crypto/tls"
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strings"
)

var IAMURL string
var AUTHURL string
var CLIENTID string
var CLIENTSECRET string
var TENANTDOMAIN string
var accessToken string
var refreshToken string
var err error

const SCOPE string ="/permission/admin/manage/identity/applicationmgt/update /permission/admin/manage/identity/applicationmgt/create /permission/admin/manage/identity/applicationmgt/view internal_application_mgt_update internal_application_mgt_create internal_application_mgt_view"

type oAuthResponse struct {
	AccessToken  string   `json:"access_token"`
	RefreshToken      string      `json:"refresh_token"`
	Scope string `json:"scope"`
	TokenType    string   `json:"token_type"`
	Expires int `json:"expires_in"`
}

func start(url string,userName string, password string){
	IAMURL=url
	AUTHURL= IAMURL+"/oauth2/token"

	accessToken,refreshToken,err=sendOAuthRequest(userName,password)
	if err != nil {
				log.Fatalln(err)
	}

	writeFiles(IAMURL,accessToken,refreshToken)
}

func sendOAuthRequest(userName string, password string) (string,string, error) {

	CLIENTID,CLIENTSECRET,TENANTDOMAIN=readSPConfig()

	var err error
	var accessToken string
	var refreshToken string
	var list oAuthResponse

	data := CLIENTID+":"+CLIENTSECRET

	// Build response body to POST :=
	body :=url.Values{}
	body.Set("grant_type","password")
	body.Set("username",userName)
	body.Set("password", password)
	body.Set("scope", SCOPE)
	body.Set("user", data)

	req, err := http.NewRequest("POST", AUTHURL,strings.NewReader(body.Encode()))
	req.SetBasicAuth(CLIENTID,CLIENTSECRET)
	req.Header.Set("Content-Type","application/x-www-form-urlencoded")
	defer req.Body.Close()

	httpClient := &http.Client{
		Transport: &http.Transport{
			                      TLSClientConfig: &tls.Config{
				                  InsecureSkipVerify: true,
				},
		},
	}

	resp, err := httpClient.Do(req)
	if err != nil {
		log.Fatalln(err)
		return accessToken,refreshToken, err
	}
	defer resp.Body.Close()

	body1, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Fatalln(err)
	}

	err2 := json.Unmarshal(body1, &list)
	if err2!=nil{
		log.Fatalln(err2)
	}

	accessToken = list.AccessToken
	refreshToken= list.RefreshToken

	return accessToken,refreshToken, err
}
