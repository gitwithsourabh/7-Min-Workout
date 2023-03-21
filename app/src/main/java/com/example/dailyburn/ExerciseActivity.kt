package com.example.dailyburn

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyburn.databinding.ActivityBackButtonPromptBinding
import com.example.dailyburn.databinding.ActivityExerciseBinding
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding :ActivityExerciseBinding? = null

    private var restimer: CountDownTimer? =null
    private var restprogress = 0

    private var exerciseTimer: CountDownTimer? =null
    private var exerciseprogress = 0

    private var restTimerDuration: Long =10
    private var exerciseTimerDuration: Long =30

    private var exerciseList : ArrayList<ExerciseModel>? =null
    private var currentExercisePosition =-1

    private var tts: TextToSpeech? = null
    private var player:MediaPlayer? =null

    private var exerciseAdapter: ExerciseStatusAdapter?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if(supportActionBar != null){                                          //Error resolved
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarExercise?.setNavigationOnClickListener{
            customDialogForBackButton()                                                 //Error resolved
        }

        exerciseList=Constants.defaultExerciseList()

        tts= TextToSpeech(this,this )


        setupRestView()
        setupExerciseStatusRecycleView()
    }


    override fun onBackPressed() {
        customDialogForBackButton()
        //super.onBackPressed()
    }

    private fun customDialogForBackButton() {
        val customDialog=Dialog(this)
        val dialogBinding = ActivityBackButtonPromptBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.btnYes.setOnClickListener{
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener{
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun setupExerciseStatusRecycleView(){
        binding?.rvExerciseName?.layoutManager=
            LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        exerciseAdapter= ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseName?.adapter = exerciseAdapter
    }



     private fun setupRestView(){

         try{                               //(error) ->music not playing
           val soundURI = Uri.parse("android.resource://com.example.dailyburn"+ R.raw.starttone)
             player=MediaPlayer.create(applicationContext,soundURI)
             player?.isLooping=false
             player?.start()
         }catch (e:Exception){
             e.printStackTrace()
         }

         binding?.flRestView?.visibility= View.VISIBLE
         binding?.tvTitle?.visibility=View.VISIBLE
         binding?.tvExercise?.visibility=View.INVISIBLE
         binding?.flExerciseView?.visibility=View.INVISIBLE
         binding?.ivImage?.visibility=View.INVISIBLE
         binding?.tvUpcomingExerciseName?.visibility=View.VISIBLE
         binding?.tvUpcomingLabel?.visibility=View.VISIBLE

         if(restimer != null){
             restimer?.cancel()
             restprogress = 0
         }

         speakOut("RELAX")
         

         binding?.tvUpcomingExerciseName?.text=exerciseList!![currentExercisePosition+1].getName()
         setRestProgressBar()
     }
    private fun setupExerciseView(){
        binding?.flRestView?.visibility= View.INVISIBLE
        binding?.tvTitle?.visibility=View.INVISIBLE
        binding?.tvExercise?.visibility=View.VISIBLE
        binding?.flExerciseView?.visibility=View.VISIBLE
        binding?.ivImage?.visibility=View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility=View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility=View.INVISIBLE
        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseprogress=0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExercise?.text=exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()

    }

    private fun setRestProgressBar(){
        binding?.progressBarExercise?.progress = restprogress

        restimer =object :CountDownTimer(restTimerDuration*1000,1000){
            override fun onTick(p0: Long) {
               restprogress++
                binding?.progressBar?.progress=10-restprogress
                binding?.textViewTimer?.text = (10-restprogress).toString()

            }

            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

               setupExerciseView()
            }

        }.start()

    }


    private fun setExerciseProgressBar(){
        binding?.progressBarExercise?.progress = exerciseprogress

         exerciseTimer=object :CountDownTimer(exerciseTimerDuration*1000,1000){
            override fun onTick(p0: Long) {
                exerciseprogress++
                binding?.progressBarExercise?.progress=30-exerciseprogress
                binding?.textViewTimerExercise?.text = (30-exerciseprogress).toString()

            }

            override fun onFinish() {



                if(currentExercisePosition<exerciseList?.size!!-1){
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                }else{
                   finish()
                    val intent =Intent(this@ExerciseActivity,FinshActivity::class.java)
                    startActivity(intent)
                }

            }

        }.start()

    }


    override fun onDestroy() {
        super.onDestroy()
        if(restimer != null){
          restimer?.cancel()
          restprogress = 0
        }

        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseprogress=0
        }

        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player!=null){
            player!!.stop()
        }


        binding = null
    }

    override fun onInit(status: Int) {
        if(status==TextToSpeech.SUCCESS){
            val result =tts?.setLanguage(Locale.US)
            if(result==TextToSpeech.LANG_MISSING_DATA || result ==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","THIS LANGUAGES NOT SUPPORTED")
            }
        }else{
            Log.e("TTS","INITIALISATION FAILED")
        }

    }

    private fun speakOut(text: String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }
}