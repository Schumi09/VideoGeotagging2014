/*

*********************************** License ************************************

Copyright 2009 Joseph Larson

This library is free software: you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.

************************************ Doco *************************************

jFunk will allow you to retrieve (and soon, manipulate) objects within complex
JSON or Javascript objects.  The design of the jFunk API will closely parallel
the jQuery API, replicating it exactly except where the differences between DOM
and Javascript make replication nonsensical.

The current script is in an extremely rudimentary, throwaway prototype form,
provided simply for exploratory purposes.

  * I would not recommend trying jFunk unless you are familiar with jQuery,
    otherwise this will not make much sense

  * This will only be appropriate with deep, complex Javascript/JSON structures

  * It has no expectation of good performance

  * The code is not "designed" currently, simply cowboy coded until it did some
    useful proof of concept things.  So its ugly, and probably buggy!

  * It does not avoid looping structures, if a parent contains a child which
    contains a reference to it's parent, a loop will occur and lock your
    browser!

However, depending on level of interest and available time, a properly
architected version will be written.

So far, only these selectors are supported:

    *
    name[atr]
    name[atr=val]
    name:first
    name:last
    #id
    .class
    parent > child

It is recommended that you always use > between known parents and their direct
children, to avoid unnecessary deep searching.

Some examples of use:

    var Food={
        fruits: [
            { name: "Banana", color: "Yellow" },
            { name: "Apple", color: "Red" },
            { name: "Grapefruit", color: "Orange" },
            { name: "Kiwi", color: "Green" }
            ],
        vegetables: [
            { name: "Carrot", color: "Orange" },
            { name: "Turnip", color: "Purple" },
            { name: "Rutabaga", color: "Yellow" },
            { name: "Sweet Potato", color: "Orange" }
            ]
        };

    var orangeStuff=jF("*[color=Orange]",Food).get();
    var orangeVeg  =Jf("> vegetables > *[color=Orange]",Food).get();

orangeStuff is now:
    [{name:"Grapefruit",color:"Orange"},{name:"Carrot",color:"Orange"},
     {name:"Sweet Potato",color:"Orange"}]

orangeVeg is now:
    [{name:"Carrot",color:"Orange"},
     {name:"Sweet Potato",color:"Orange"}]

*/

var jFunk=function(sch,obj) {
    if(!obj) { return []; }
    var cmaspl=sch.split(",");
    var rtn=[];
    for(var ci=0; ci<cmaspl.length; ci++) {
        var spcspl=cmaspl[ci].split(" ");
        var sub=[obj];

        if(cmaspl[ci].indexOf("(")>-1) {
            for(var si=0; si<spcspl.length; si++) {
                if(spcspl[si].indexOf("(")>-1) {
                    var frs=true;
                    var cls=si+1;
                    while(cls<spcspl.length && spcspl[cls]!=")") {
                        cls++;
                        }
                    spcspl[si]+=" "+spcspl.splice(si+1,cls-si-1).join(" ");
                    }
                }
            }

        for(var si=0; si<spcspl.length; si++) {
            if(spcspl[si]==">") {
                si++;
                sub=jFunk.sub.find(spcspl[si],sub,1);
                }
            else {
                sub=jFunk.sub.find(spcspl[si],sub);
                }
            }
        rtn=rtn.concat(sub);
        }
    return {
        results: rtn,
        get:function(idx) {
            if(typeof idx!="number") { return this.results; }
            else { return this.results[idx]; }
            }
        };
    }
jFunk.init=function(prm) {
    for(pi in prm) {
        jFunk.sub[pi]=prm[pi];
        }
    }
jFunk.sub={
    propertyPound:"id",
    propertyDot:"class",
    find:function(sch,arr,chl) {
        if(typeof sch!="number" && sch.length==0) { return obj; }
        var colspl=sch.split(":");
        var brkspl=colspl[0].split("[");
        var flt=colspl[1];//presumes single filter;
        sch=brkspl.shift();
        var prp=brkspl;
        for(var pi=0; pi<prp.length; pi++) {
            prp[pi]=prp[pi].split("]")[0].split("=");
            }
        var num=Number(sch);
        if(!isNaN(num)) { sch=num; }

        //var typ=(flt=="object" || flt=="string" || flt=="number" || flt=="

        var rtn=[];
        for(var ai=0; ai<arr.length; ai++) {
            if(typeof arr[ai]!="object") { continue; }
            rtn=rtn.concat(this.find_sub(sch,arr[ai],prp,chl));
            }
        if(flt) {
            if(flt=="first")      { return [rtn[0]]; }
            else if(flt=="last" ) { return [rtn.pop()];  }
            else if(flt.indexOf("has")>-1) {
                return this.find_has(rtn,flt);
                }
            else { return rtn; }
            }
        else {
            return rtn;
            }
        },
    find_sub:function(sch,obj,prp,chl) {
        if(typeof obj!="object") { return []; }
        var rtn=[];
        if(typeof sch=="number") {
            if(obj[sch]) { if(!prp || this.find_matchProperty(obj[sch],prp)) { rtn.push(obj[sch]); } }
            }
        else if(sch.charAt(0)=="#" && obj[jFunk.sub.propertyPound]) {
            if(obj[jFunk.sub.propertyPound]==sch.split("#")[1]) { if(!prp || this.find_matchProperty(obj,prp)) { rtn.push(obj); } }
            }
        else if(sch.charAt(0)=="." && obj[jFunk.sub.propertyDot]) {
            if(obj[jFunk.sub.propertyDot]==sch.split(".")[1]) { if(!prp || this.find_matchProperty(obj,prp)) { rtn.push(obj); } }
            }
        else if(sch=="*") {
            for(pr in obj) { if(!prp || this.find_matchProperty(obj[pr],prp)) { rtn.push(obj[pr]); } }
            }
        else if(sch!="*") {
            if(obj[sch]) { if(!prp || this.find_matchProperty(obj[sch],prp)) { rtn.push(obj[sch]); } }
            }
        if(typeof obj=="object" && (!chl || (chl==1 && obj.sort)) && !obj.innerHTML) {
            for(pr in obj) { rtn=rtn.concat(jFunk.sub.find_sub(sch,obj[pr],prp,( chl ? 2 : null )));  }
            }
        return rtn;
        },
    find_matchProperty:function(obj,prp) {
        if(!prp) { return true; }
        for(var pi=0; pi<prp.length; pi++) {
            if(prp[pi].length==1 && typeof obj[prp[pi][0]]=="undefined") { return false; }
            if(prp[pi].length==2 && prp[pi][1]!=obj[prp[pi][0]]) { return false; }
            }
        return true;
        },
    find_has:function(arr,has) {
        var sch=has.split("has(")[1].split(")")[0];
        for(var ai=0; ai<arr.length; ai++) {
            var rtn=jFunk(sch,arr[ai]);
            if(!rtn || rtn.length==0) {
                arr.splice(ai,1);
                ai--;
                }
            }
        return arr;
        }
    }


var jF=jFunk;